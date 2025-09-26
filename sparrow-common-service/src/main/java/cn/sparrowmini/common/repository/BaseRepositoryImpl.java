package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.antlr.PredicateBuilder;
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.reflections.ReflectionUtils;
import org.springframework.data.domain.*;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
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
    @NonNull
    public Field idField() {
        final Class<T> entityClass = domainType();
        for (Field f : ReflectionUtils.getAllFields(entityClass)) {
            if (f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(EmbeddedId.class)) {
                return f;
            }
        }
        throw new RuntimeException("找不到ID字段: " + entityClass.getName());
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

    /**
     * 主方法：分页查询 + 动态 DTO 投影 + 集合递归
     */
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
        List<Selection<?>> selections = DynamicProjectionHelper.buildSelections(
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
        final List<P> finalResult = new ArrayList<>();
        // 转换成 DTO，支持嵌套对象，这个为主表的对象，不含子集合的查询，但是对于非集合，则直接join出来了
        List<Map<String,Object>> results = tuples.stream()
                .map(tuple -> {
                    Map<String, Object> tupleMap = new HashMap<>();
                    Map<String, Map<String, Object>> nestedMaps = new HashMap<>();

                    for (TupleElement<?> elem : tuple.getElements()) {
                        String alias = elem.getAlias();
                        Object value = tuple.get(elem);

                        if (alias.contains(".")) {
                            String[] parts = alias.split("\\.", 2);
                            nestedMaps.computeIfAbsent(parts[0], k -> new HashMap<>())
                                    .put(parts[1], value);
                        } else {
                            tupleMap.put(alias, value);
                        }
                    }

                    nestedMaps.forEach(tupleMap::put);
                    if(projectionClass.isInterface()){
                        finalResult.add(projectionFactory.createProjection(projectionClass, tupleMap));
                    }
                    return tupleMap;
                })
                .collect(Collectors.toList());

        //递归处理含有子集合的数据
        if(!projectionClass.isInterface()){
            boolean hasCollectionField = Arrays.stream(projectionClass.getDeclaredFields())
                    .anyMatch(f -> Collection.class.isAssignableFrom(f.getType()));

            if (hasCollectionField) {
                // ----------------------------
                // Step 3: 加载集合字段
                // ----------------------------
                DynamicProjectionHelper.loadCollectionsV2(results, domainType(), projectionClass, em, idField());
            }
            ObjectMapper mapper = JsonUtils.getMapper();
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, projectionClass);
            List<P> list = mapper.convertValue(results, type);
            finalResult.addAll(list);
        }


        // Count 查询
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(domainType());
        Predicate countPredicate = spec != null ? spec.toPredicate(countRoot, countQuery, cb)
                : filter != null ? PredicateBuilder.buildPredicate(filter, cb, countRoot)
                : predicate != null ? predicate
                : cb.conjunction();
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicate);
        TypedQuery<Long> countTypedQuery = em.createQuery(countQuery);

        return PageableExecutionUtils.getPage(finalResult, pageable, countTypedQuery::getSingleResult);
    }



    /**
     * 递归填充集合关联字段
     */
    private <E> void populateCollectionField(Map<Object, E> parentEntities, Field collectionField,
                                             Map<Class<?>, Map<Object, List<?>>> cache) {
        Class<?> elementType = getCollectionGenericClass(collectionField);
        if (parentEntities.isEmpty()) return;

        List<Object> parentIds = parentEntities.keySet().stream().toList();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(elementType);
        Root<?> root = query.from(elementType);

        Field parentRefField = findParentReferenceField(elementType, collectionField.getDeclaringClass());
        query.where(root.get(parentRefField.getName()).in(parentIds));
        List<?> children = em.createQuery(query).getResultList();

//        Map<Object, List<?>> grouped = children.stream()
//                .collect(Collectors.groupingBy(c -> getFieldValue(c, parentRefField), LinkedHashMap::new, Collectors.toList()));
        var grouped = children.stream()
                .collect(Collectors.groupingBy(
                        c -> getFieldValue(c, parentRefField),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 递归子集合
        for (Field f : elementType.getDeclaredFields()) {
            if (Collection.class.isAssignableFrom(f.getType()) && isAssociation(f)) {
                populateCollectionField(grouped, f, cache);
            }
        }
    }

    /**
     * 通过反射获取字段值，支持复合ID
     */
    private Object getFieldValue(Object entity, Field field) {
        try {
            field.setAccessible(true);
            Object val = field.get(entity);
            if (val == null) return null;

            // 复合ID
            if (isCompositeId(val.getClass())) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (Field sub : val.getClass().getDeclaredFields()) {
                    sub.setAccessible(true);
                    map.put(sub.getName(), sub.get(val));
                }
                // 转为字符串 key
                return map.toString();
            }

            // 如果是实体对象，则取其 ID 字段
            if (isEntity(val.getClass())) {
                Field idField = findIdField(val.getClass());
                return getFieldValue(val, idField);
            }

            return val;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEntity(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class);
    }

    private boolean isCompositeId(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .anyMatch(f -> !f.getName().equals("serialVersionUID"));
    }

    /**
     * 查找实体主键字段
     */
    private Field findIdField(Class<?> entityClass) {
       for (Field f : ReflectionUtils.getAllFields(entityClass)) {
            if (f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(EmbeddedId.class)) {
                return f;
            }
        }
        throw new RuntimeException("找不到ID字段: " + entityClass.getName());
    }

    /**
     * 获取集合泛型类型
     */
    private Class<?> getCollectionGenericClass(Field f) {
        Type genericType = f.getGenericType();
        if (genericType instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1) {
                return (Class<?>) args[0];
            }
        }
        return Object.class;
    }

    /**
     * 动态推断 DTO 集合元素类型
     */
    private Class<?> getCollectionElementProjectionClass(Field f) {
        // 尝试使用注解或命名规则，若找不到则默认 Object.class
        // 可根据需要扩展，比如 @DtoClass 注解
        return getCollectionGenericClass(f);
    }

    /**
     * 判断是否关联字段（@ManyToOne / @OneToMany / @OneToOne / @ManyToMany）
     */
    private boolean isAssociation(Field f) {
        return f.isAnnotationPresent(OneToMany.class)
                || f.isAnnotationPresent(ManyToOne.class)
                || f.isAnnotationPresent(OneToOne.class)
                || f.isAnnotationPresent(ManyToMany.class);
    }

    /**
     * 查找子表关联回父表的字段
     */
    private Field findParentReferenceField(Class<?> childClass, Class<?> parentClass) {
        for (Field f : childClass.getDeclaredFields()) {
            if (f.getType().equals(parentClass) && isAssociation(f)) {
                return f;
            }
        }
        throw new RuntimeException("找不到子表回父表的关联字段: " + childClass.getName());
    }

    // -----------------------------
// 泛型反射动态获取集合元素类型
// -----------------------------
    private Class<?> getCollectionElementType(Field f) {
        Type genericType = f.getGenericType();
        if (genericType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length == 1) {
                Type elementType = typeArgs[0];
                if (elementType instanceof Class<?> clazz) {
                    return clazz;
                } else if (elementType instanceof ParameterizedType pt) {
                    return (Class<?>) pt.getRawType(); // 支持嵌套泛型
                }
            }
        }
        return Object.class;
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