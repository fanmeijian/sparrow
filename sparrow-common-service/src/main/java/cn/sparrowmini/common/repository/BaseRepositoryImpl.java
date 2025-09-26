package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.antlr.PredicateBuilder;
import cn.sparrowmini.common.util.JsonUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class BaseRepositoryImpl<T, ID>
        extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final EntityManager em;
    private final Class<T> domainClass;
    private final JpaEntityInformation<T, ID> entityInformation;
    private final ProjectionFactory projectionFactory;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em, ProjectionFactory projectionFactory) {
        super(entityInformation, em);
        this.domainClass = entityInformation.getJavaType();
        this.em = em;
        this.entityInformation = (JpaEntityInformation<T, ID>) entityInformation;
        this.projectionFactory = projectionFactory;
    }

    @Override
    public <S> Optional<S> findByIdProjection(ID id, Class<S> projectionClass) {
        String idField = idFieldName();
        Specification<T> spec = (root, query, cb) -> cb.equal(root.get(idField), id);
        return findByProjection(PageRequest.of(0,1),spec,projectionClass).stream().findFirst();
//        return findBy(spec, q -> q.as(projectionClass).first());
    }


    @NonNull
    @Override
    public Class<T> domainType() {
        return this.domainClass;
    }

    @Override
    @NonNull
    public Class<ID> idType() {
        return entityInformation.getIdType();
    }

    @Override
    @NonNull
    public String idFieldName() {
        return entityInformation.getIdAttributeNames()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No @Id field found"));
    }


    @Override
    public <P> Page<P> findByProjection(Pageable pageable, Specification<T> spec, Class<P> projectionClass) {
        return findByProjectionInternal(pageable, spec, null, projectionClass);
    }

    @Override
    public <P> Page<P> findByProjection(Pageable pageable, Predicate predicate, Class<P> projectionClass) {
        return findByProjectionInternal(pageable, null, predicate, projectionClass);
    }

    @Override
    public <P> Page<P> findByProjection(Pageable pageable, String filter, Class<P> projectionClass) {
        return findByProjectionInternal(pageable, null, null, projectionClass, filter);
    }

    /**
     * 内部统一方法，处理 Specification / Predicate / filter
     */
    private <P> Page<P> findByProjectionInternal(Pageable pageable,
                                                 Specification<T> spec,
                                                 Predicate predicate,
                                                 Class<P> projectionClass) {
        return findByProjectionInternal(pageable, spec, predicate, projectionClass, null);
    }

    private <P> Page<P> findByProjectionInternal(Pageable pageable,
                                                 Specification<T> spec,
                                                 Predicate predicate,
                                                 Class<P> projectionClass,
                                                 String filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<T> root = query.from(domainType());

        // 构建 Predicate
        Predicate finalPredicate = predicate != null ? predicate
                : spec != null ? spec.toPredicate(root, query, cb)
                : filter != null ? PredicateBuilder.buildPredicate(filter, cb, root)
                : cb.conjunction();

        // 构建 select 投影字段
        List<Selection<?>> selections = DynamicProjectionHelper.buildSelectionsV2(
                domainType(), cb, root, projectionClass, "", new HashMap<>()
        );
        query.multiselect(selections);
        query.where(finalPredicate);

        // 排序
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, cb));
        }

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        if (pageable.isPaged()) {
            typedQuery.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
            typedQuery.setMaxResults(pageable.getPageSize());
        }

        List<Tuple> tuples = typedQuery.getResultList();

        // -------------------------
        // 聚合主表 + 嵌套对象 + 集合
        // -------------------------
        Map<Object, P> aggregated = new LinkedHashMap<>();
        String idField = "id"; // 主表 ID 字段名，根据情况修改

        for (Tuple tuple : tuples) {
            Object mainId = tuple.get(idField);
            P dto = aggregated.computeIfAbsent(mainId, k -> {
                Map<String, Object> baseMap = new HashMap<>();
                tuple.getElements().forEach(e -> {
                    String alias = e.getAlias();
                    if (!alias.contains(".")) {
                        baseMap.put(alias, tuple.get(e));
                    }
                });
                if (projectionClass.isInterface()) {
                    return projectionFactory.createProjection(projectionClass, baseMap);
                } else {
                    return JsonUtils.getMapper().convertValue(baseMap, projectionClass);
                }
            });

            // 处理嵌套对象
            Map<String, Map<String, Object>> nestedMaps = new HashMap<>();
            tuple.getElements().forEach(e -> {
                String alias = e.getAlias();
                Object value = tuple.get(e);
                if (alias.contains(".")) {
                    String[] parts = alias.split("\\.", 2);
                    nestedMaps.computeIfAbsent(parts[0], k -> new HashMap<>())
                            .put(parts[1], value);
                }
            });

            nestedMaps.forEach((prop, map) -> {
                try {
                    Field f = projectionClass.getDeclaredField(prop);
                    f.setAccessible(true);
                    Object nestedValue;
                    if (Collection.class.isAssignableFrom(f.getType())) {
                        Collection<Object> coll = (Collection<Object>) f.get(dto);
                        if (coll == null) {
                            coll = new LinkedHashSet<>();
                            f.set(dto, coll);
                        }
                        Object element = JsonUtils.getMapper().convertValue(map, getCollectionGenericClass(f));
                        coll.add(element);
                    } else {
                        nestedValue = JsonUtils.getMapper().convertValue(map, f.getType());
                        f.set(dto, nestedValue);
                    }
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                }
            });
        }

        List<P> results = new ArrayList<>(aggregated.values());

        // -------------------------
        // Count 查询
        // -------------------------
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(domainType());
        Predicate countPredicate = spec != null ? spec.toPredicate(countRoot, countQuery, cb)
                : filter != null ? PredicateBuilder.buildPredicate(filter, cb, countRoot)
                : predicate != null ? predicate
                : cb.conjunction();
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicate);
        TypedQuery<Long> countTypedQuery = em.createQuery(countQuery);

        return PageableExecutionUtils.getPage(results, pageable, countTypedQuery::getSingleResult);
    }

    // -------------------------
// 工具方法：获取集合泛型
// -------------------------
    @SuppressWarnings("unchecked")
    private static Class<?> getCollectionGenericClass(Field field) {
        try {
            return (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType())
                    .getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new IllegalStateException("无法获取集合泛型类型: " + field.getName(), e);
        }
    }



//
//    private <P> TypedQuery<Tuple> buildQuery(Class<P> projectionClass, Pageable pageable,@NonNull Predicate predicate) {
//        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<Tuple> query = cb.createTupleQuery();
//        Root<?> root = query.from(domainType());
//
//        return buildQuery(projectionClass,pageable,cb,root,predicate);
//    }
//
//    private <P> TypedQuery<Tuple> buildQuery(Class<P> projectionClass, Pageable pageable,CriteriaBuilder cb,Root<?> root,@NonNull Predicate predicate) {
//        CriteriaQuery<Tuple> query = cb.createTupleQuery();
//
//        // 构建select投影字段
//
//        List<Selection<?>> selections = DynamicProjectionHelper.buildSelections(domainType(), cb, root, projectionClass, "", new HashMap<>());
//        query.multiselect(selections);
//        query.where(predicate);
//
//        // 排序
//        Sort sort = pageable.getSort();
//        if (sort.isSorted()) {
//            query.orderBy(QueryUtils.toOrders(sort, root, cb));
//        }
//
//        return em.createQuery(query);
//    }
//
//
//    private static boolean isJavaStandardType(Class<?> clazz) {
//        final Set<Class<?>> JAVA_TIME_TYPES = Set.of(
//                java.time.LocalDate.class,
//                java.time.LocalDateTime.class,
//                java.time.OffsetDateTime.class,
//                java.time.Instant.class,
//                java.time.ZonedDateTime.class,
//                java.time.OffsetTime.class,
//                java.time.LocalTime.class,
//                java.time.Duration.class,
//                java.time.Period.class
//        );
//        return clazz.isPrimitive()
//                || clazz.getName().startsWith("java.lang.")
//                || clazz.equals(String.class)
//                || Number.class.isAssignableFrom(clazz)
//                || Date.class.isAssignableFrom(clazz)
//                || clazz.isEnum()
//                || JAVA_TIME_TYPES.contains(clazz);
//    }
//
//    @Override
//    public <P> Page<P> findByProjection(Pageable pageable, Specification<T> spec, Class<P> projectionClass){
//        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<Tuple> query = cb.createTupleQuery();
//        Root<T> root = query.from(domainType());
//        return findByProjection(pageable,spec==null?cb.conjunction(): spec.toPredicate(root, query, cb),projectionClass);
//    }
//
//    @Override
//    public <P> Page<P> findByProjection(Pageable pageable, Predicate predicate, Class<P> projectionClass) {
//
//        TypedQuery<Tuple> query = buildQuery(projectionClass, pageable, predicate);
//        if (pageable.isPaged()) {
//            query.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
//            query.setMaxResults(pageable.getPageSize());
//        }
//
//        List<Tuple> tuples = query.getResultList();
//
//        List<P> results = tuples.stream()
//                .map(tuple -> {
//                    // 把Tuple转成Map或自定义MapLike结构
//                    Map<String, Object> tupleMap = new HashMap<>();
//                    for (TupleElement<?> elem : tuple.getElements()) {
//                        tupleMap.put(elem.getAlias(), tuple.get(elem));
//                    }
//                    // 动态代理生成接口实例
//                    if(projectionClass.isInterface()){
//                        return projectionFactory.createProjection(projectionClass, tupleMap);
//                    }else{
//                        return JsonUtils.getMapper().convertValue(tupleMap,projectionClass);
//                    }
//
//                })
//                .collect(Collectors.toList());
//
//        return PageableExecutionUtils.getPage(results, pageable, () -> {
//            return this.getCountQuery(predicate, domainType()).getSingleResult();
//        });
//    }
//
//    @Override
//    public <P> Page<P> findByProjection(Pageable pageable, String filter, Class<P> projectionClass) {
//        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<Tuple> query = cb.createTupleQuery();
//        Root<?> root = query.from(domainType());
//        Predicate predicate = PredicateBuilder.buildPredicate(filter,cb,root);
//        return findByProjection(pageable,predicate,projectionClass);
//    }
//
//
//    private <T> TypedQuery<Long> getCountQuery(Predicate predicate, Class<T> domainClass) {
//        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
//        Root<?> countRoot = countQuery.from(domainClass);
//        countQuery.select(cb.count(countRoot));
//        countQuery.where(predicate==null?cb.conjunction(): predicate);
//        return em.createQuery(countQuery);
//    }
//
//
//    private String[] getPropertyNames(Class<?> projectionClass) {
//        List<String> paths = extractPropertyPaths(projectionClass, "");
//        System.out.println("Projection fields: " + Arrays.toString(paths.toArray(new String[0])));
//        return paths.toArray(new String[0]);
//    }
//
//    private List<String> extractPropertyPaths(Class<?> projectionClass, String prefix) {
//        List<String> props = new ArrayList<>();
//
//        if (projectionClass.isInterface()) {
//            for (Method method : projectionClass.getMethods()) {
//                if (method.getParameterCount() != 0) continue;
//                String methodName = method.getName();
//                if (!(methodName.startsWith("get") || methodName.startsWith("is"))) continue;
//
//                String propName = methodName.startsWith("get")
//                        ? methodName.substring(3)
//                        : methodName.substring(2);
//                propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
//
//                Class<?> returnType = method.getReturnType();
//
//                if (isJavaStandardType(returnType)) {
//                    props.add(prefix + propName);
//                } else {
//                    // 递归处理嵌套对象
//                    props.addAll(extractPropertyPaths(returnType, prefix + propName + "."));
//                }
//            }
//        } else {
//            // DTO类，直接字段，不递归（如果你需要，也可以实现）
//            for (Field field : projectionClass.getDeclaredFields()) {
//                props.add(prefix + field.getName());
//            }
//        }
//
//        return props;
//    }

}