package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.antlr.PredicateBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
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
        return findBy(spec, q -> q.as(projectionClass).first());
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

    public <P> TypedQuery<Tuple> buildQuery(Class<P> projectionClass, Pageable pageable, String filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<?> root = query.from(domainType());

        // 构建select投影字段
        List<Selection<?>> selections = buildSelections(cb, root, projectionClass, "", new HashMap<>());
        query.multiselect(selections);

        // 条件过滤
        if (filter != null && !filter.isBlank()) {
            query.where(PredicateBuilder.buildPredicate(filter, cb, root));
        }

        // 排序
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, cb));
        }

        return em.createQuery(query);
    }


    private List<Selection<?>> buildSelections(CriteriaBuilder cb, From<?, ?> root,
                                               Class<?> projectionClass, String prefix,
                                               Map<String, From<?, ?>> joins) {

        System.out.println(extractPropertyPaths(projectionClass,""));
        List<Selection<?>> selections = new ArrayList<>();

        if (projectionClass.isInterface()) {
            for (Method method : projectionClass.getMethods()) {
                if (method.getParameterCount() != 0) continue;
                String methodName = method.getName();
                if (!(methodName.startsWith("get") || methodName.startsWith("is"))) continue;

                String propName = methodName.startsWith("get")
                        ? methodName.substring(3)
                        : methodName.substring(2);
                propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);

                Class<?> returnType = method.getReturnType();

                if (isJavaStandardType(returnType)) {
                    selections.add(root.get(prefix + propName).alias(prefix + propName));
                } else {
                    // 递归处理嵌套对象
                    // 复杂类型需要join
                    String joinPath = prefix + propName;
                    From<?, ?> join = joins.get(joinPath);
                    if (join == null) {
                        join = root.join(propName, JoinType.LEFT);
                        joins.put(joinPath, join);
                    }
                    // 递归处理子属性，prefix清空因为join已经定位了路径
                    selections.addAll(buildSelections(cb, join, returnType, "", joins));
                }
            }
        } else {
            for (Field field : domainType().getDeclaredFields()) {
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();

                if (isJavaStandardType(fieldType)) {
                    // 简单类型直接选字段
                    selections.add(root.get(prefix + fieldName).alias(prefix + fieldName));
                } else {
                    // 复杂类型需要join
                    String joinPath = prefix + fieldName;
                    From<?, ?> join = joins.get(joinPath);
                    if (join == null) {
                        join = root.join(fieldName, JoinType.LEFT);
                        joins.put(joinPath, join);
                    }
                    // 递归处理子属性，prefix清空因为join已经定位了路径
                    selections.addAll(buildSelections(cb, join, fieldType, "", joins));
                }
            }
        }


        return selections;
    }

    private static boolean isJavaStandardType(Class<?> clazz) {
        final Set<Class<?>> JAVA_TIME_TYPES = Set.of(
                java.time.LocalDate.class,
                java.time.LocalDateTime.class,
                java.time.OffsetDateTime.class,
                java.time.Instant.class,
                java.time.ZonedDateTime.class,
                java.time.OffsetTime.class,
                java.time.LocalTime.class,
                java.time.Duration.class,
                java.time.Period.class
        );
        return clazz.isPrimitive()
                || clazz.getName().startsWith("java.lang.")
                || clazz.equals(String.class)
                || Number.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz)
                || clazz.isEnum()
                || JAVA_TIME_TYPES.contains(clazz);
    }



    @Override
    public <P> Page<P> findAllProjection(Pageable pageable, String filter, Class<P> projectionClass) {
        TypedQuery<Tuple> query = buildQuery(projectionClass, pageable, filter);
        if (pageable.isPaged()) {
            query.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
            query.setMaxResults(pageable.getPageSize());
        }

        List<Tuple> tuples = query.getResultList();

        List<P> results = tuples.stream()
                .map(tuple -> {
                    // 把Tuple转成Map或自定义MapLike结构
                    Map<String, Object> tupleMap = new HashMap<>();
                    for (TupleElement<?> elem : tuple.getElements()) {
                        tupleMap.put(elem.getAlias(), tuple.get(elem));
                    }
                    // 动态代理生成接口实例
                    return projectionFactory.createProjection(projectionClass, tupleMap);
                })
                .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(results, pageable, () -> {
            return this.getCountQuery(filter, domainType()).getSingleResult();
        });
    }

    private <T> TypedQuery<T> getQuery(String filter, Pageable pageable, Class<T> domainClass) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(domainClass);
        Root<T> root = query.from(domainClass);
        if (filter != null && !filter.isBlank()) {
            query.where(PredicateBuilder.buildPredicate(filter, builder, root));
        }

        query.select(root);
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }
        return em.createQuery(query);
    }

    private <T> TypedQuery<Long> getCountQuery(String filter, Class<T> domainClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<?> countRoot = countQuery.from(domainClass);
        countQuery.select(cb.count(countRoot));
        if (filter != null && !filter.isBlank()) {
            countQuery.where(PredicateBuilder.buildPredicate(filter, cb, countRoot));
        }
        return em.createQuery(countQuery);
    }

    private String[] getPropertyNames(Class<?> projectionClass) {
        List<String> paths = extractPropertyPaths(projectionClass, "");
        System.out.println("Projection fields: " + Arrays.toString(paths.toArray(new String[0])));
        return paths.toArray(new String[0]);
    }

    private List<String> extractPropertyPaths(Class<?> projectionClass, String prefix) {
        List<String> props = new ArrayList<>();

        if (projectionClass.isInterface()) {
            for (Method method : projectionClass.getMethods()) {
                if (method.getParameterCount() != 0) continue;
                String methodName = method.getName();
                if (!(methodName.startsWith("get") || methodName.startsWith("is"))) continue;

                String propName = methodName.startsWith("get")
                        ? methodName.substring(3)
                        : methodName.substring(2);
                propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);

                Class<?> returnType = method.getReturnType();

                if (isJavaStandardType(returnType)) {
                    props.add(prefix + propName);
                } else {
                    // 递归处理嵌套对象
                    props.addAll(extractPropertyPaths(returnType, prefix + propName + "."));
                }
            }
        } else {
            // DTO类，直接字段，不递归（如果你需要，也可以实现）
            for (Field field : projectionClass.getDeclaredFields()) {
                props.add(prefix + field.getName());
            }
        }

        return props;
    }

}