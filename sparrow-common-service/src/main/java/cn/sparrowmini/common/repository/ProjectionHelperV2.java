package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.util.JsonUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ProjectionHelperV2 {
    private static EntityManager entityManager;

    /**
     * 所有非集合字段都可以一次性全部获取出来
     */
    public static List<Selection<?>> buildEntitySelection(Root<?> root, Class<?> entityClass, Class<?> projectClass) {
        Map<String, Field> entityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(entityClass);
        Map<String, Field> projectFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(projectClass);
        Collection<Field> entityFields = entityFieldsMap.values();
        Collection<Field> projectFields = projectFieldsMap.values();


        List<Selection<?>> selections = new ArrayList<>();
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
//        Root<?> root = tupleQuery.from(entityClass);

        //一次性构建所有的selection
        for (Field projectField : projectFields) {
            if (ProjectionHelperUtil.isCollectionField(projectField.getType())) continue;

            String projectFieldName = projectField.getName();
            Field entityField = entityFieldsMap.get(projectFieldName);
            if (entityField == null) continue;

            String entityFieldName = entityField.getName();
            Class<?> entityFieldClass = entityField.getType();
            Class<?> projectFieldClass = projectField.getType();

            //标准字段
            if (ProjectionHelperUtil.isJavaStandardType(projectField)) {
                selections.add(root.get(entityFieldName).alias(entityFieldName));
            }

            //嵌入字段
            //embedded字段
            if (ProjectionHelperUtil.isEmbedded(entityField)) {
                //要递归
                log.info("递归嵌入字段 {} {} {}", entityField.getName(), entityFieldClass.getName(), projectFieldClass.getName());
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(root, entityFieldName);
                selections.addAll(buildEmbeddedSelection(entityFieldName, join, entityFieldClass, projectFieldClass));
            }

            //toOne字段
            if (ProjectionHelperUtil.isAssociationOne(entityField)) {
                //toOne字段要找到@JoinColumn的name属性，从而获取真正的字段
                JoinColumn joinColumn = entityField.getAnnotation(JoinColumn.class);
                String joinColumnName = joinColumn.name();

                //递归处理
                //要递归
                log.info("递归关联字段 {} {} {}", entityField.getName(), entityFieldClass.getName(), projectFieldClass.getName());
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(root, entityFieldName);
                selections.addAll(buildSelection(entityFieldName, join, entityFieldClass, projectFieldClass));
            }
        }
        return selections;
    }

    /**
     * 获取实体类信息，去除集合的字段
     *
     * @param parentIds
     * @param parentEntityClass
     * @param entityClass
     * @param projectClass
     * @param em
     * @return
     */
    public static List<Tuple> projectEntity(Collection<Object> parentIds, Class<?> parentEntityClass, Class<?> entityClass, Class<?> projectClass, EntityManager em) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<?> root = cq.from(entityClass);


        //获取子集合类的所有非集合的投影字段
        List<Selection<?>> selections = buildEntitySelection(root, entityClass, projectClass);
        Field parentRefField = ProjectionHelperUtil.getReferenceParentField(entityClass, parentEntityClass);
        Field parentIdField = ProjectionHelperUtil.findIdField(parentEntityClass);
        Class<?> parentIdClass = parentIdField.getType();
        final Path<?> parentPath = root.get(parentRefField.getName());
        if (selections.stream().noneMatch(s -> s.getAlias().startsWith(parentRefField.getName() + "."))) {
            selections.add(parentPath.get(parentIdField.getName()).alias(parentRefField.getName() + "." + parentIdField.getName()));
        }
        cq.multiselect(selections);
        Predicate predicate = buildPredicate(parentIds, entityClass, parentEntityClass, root, em);
        cq.where(predicate);
        return em.createQuery(cq).getResultList();
    }

    public static void projectCollectionV2(Map<Object, Map<String, Object>> parentEntitiesByIdMap, Class<?> parentEntityClass, Class<?> entityClass, Class<?> projectClass, EntityManager em, String fieldNameInParent) {
        //根据父id，先获取到自己的所有非集合对象
        Map<String, Field> entityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(entityClass);
        Map<String, Field> projectFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(projectClass);
        Map<String, Field> parentEntityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(parentEntityClass);

        Collection<Field> entityFields = entityFieldsMap.values();
        Collection<Field> projectFields = projectFieldsMap.values();
        Collection<Object> parentIds = parentEntitiesByIdMap.keySet();

        boolean isCollectionFieldOfParent = ProjectionHelperUtil.isCollectionField(parentEntityFieldsMap.get(fieldNameInParent).getType());
        //获取到自己的id集合
        Map<Object, Map<String, Object>> childEntitiesByIdMap = Map.of();
        Collection<Object> childIds = new HashSet<>();
        //只有自己是父的集合的时候，才需要到数据库根据父id，查询出来project，如果不是集合，则在父类中已经通过join查询出来了，直接获取他的列表值即可
        if (isCollectionFieldOfParent) {
            List<Tuple> childTuples = projectEntity(parentIds, parentEntityClass, entityClass, projectClass, em);
            List<Map<String, Object>> collectionEntities = ProjectionHelperUtil.tuplesToMap(childTuples);
            childEntitiesByIdMap = keyById(collectionEntities, entityClass);
            childIds = childEntitiesByIdMap.keySet();
            String parentRefFieldName = ProjectionHelperUtil.getParentReferenceField(entityClass, parentEntityClass);
            Map<Object, List<Map<String, Object>>> groupedCollections = groupByParentId(collectionEntities, parentRefFieldName, parentIds);
            //放回大树
            mergeToParent(groupedCollections, parentEntitiesByIdMap, parentIds, parentEntityFieldsMap.get(fieldNameInParent));
        } else {
            List<Map<String, Object>> childEntities = getAllByKey(parentEntitiesByIdMap.values(), fieldNameInParent);//JsonPath.read(parentEntitiesByIdMap.values(),String.join(".","$[*]",fieldNameInParent));
            childEntitiesByIdMap = keyById(childEntities, entityClass);
        }


        //再递归所有的子集合对象
        for (Field projectField : projectFields) {
            String projectFieldName = projectField.getName();
            Class<?> projectFieldClass = projectField.getType();
            Field entityField = entityFieldsMap.get(projectFieldName);
            if (entityField == null) continue;

            if (ProjectionHelperUtil.isCollectionField(entityField.getType())) {
                Class<?> collectionEntityClass = ProjectionHelperUtil.getCollectionGenericClass(entityField);
                Class<?> collectionProjectClass = ProjectionHelperUtil.getCollectionGenericClass(projectField);
                log.info("递归子集合 {}", projectFieldName);
                projectCollectionV2(childEntitiesByIdMap, entityClass, collectionEntityClass, collectionProjectClass, em, projectFieldName);
            } else if (ProjectionHelperUtil.isAssociationOne(entityField)) {
                log.info("递归关联实体 {}", projectFieldName);
                projectCollectionV2(childEntitiesByIdMap, entityClass, entityField.getType(), projectFieldClass, em, projectFieldName);
            }
        }
    }

    static Map<Object, Map<String, Object>> keyById(List<Map<String, Object>> entityList, Class<?> entityClass) {
        Field idField = ProjectionHelperUtil.findIdField(entityClass);
        Map<Object, Map<String, Object>> map = new HashMap<>();
        entityList.forEach(f -> {
            Object id = f.get(idField.getName());
            map.put(id, f);
        });
        return map;
    }

    static List<Map<String, Object>> getAllByKey(Collection<Map<String, Object>> entityList, String fieldName) {
        return entityList.stream().map(entity -> (Map<String, Object>) entity.get(fieldName)).filter(Objects::nonNull).toList();
    }


    public static void projectCollection(Map<Object, Map<String, Object>> parentEntitiesById, Class<?> parentEntityClass, Class<?> parentProjectClass, String collectionFieldName, EntityManager em, String prefix) {
        Set<Object> parentIds = parentEntitiesById.keySet();
        log.info("获取子集合 {} 父类 {} 父DTO {} 父id长度 {} ", String.join(".", prefix, collectionFieldName), parentEntityClass.getName(), parentProjectClass.getName(), parentEntitiesById.size());
        Map<String, Field> parentEntityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(parentEntityClass);
        Map<String, Field> parentProjectFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(parentProjectClass);
        Collection<Field> parentEntityFields = parentEntityFieldsMap.values();
        Collection<Field> parentProjectFields = parentProjectFieldsMap.values();

        Field collectionProjectField = parentProjectFieldsMap.get(collectionFieldName);
        Field collectionEntityField = parentEntityFieldsMap.get(collectionFieldName);

        Class<?> collectionEntityClass = ProjectionHelperUtil.getCollectionGenericClass(collectionEntityField);
        Class<?> collectionProjectClass = ProjectionHelperUtil.getCollectionGenericClass(collectionProjectField);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<?> root = cq.from(collectionEntityClass);


        //获取子集合类的所有非集合的投影字段
        List<Selection<?>> selections = buildEntitySelection(root, collectionEntityClass, collectionProjectClass);
        Field parentRefField = ProjectionHelperUtil.getReferenceParentField(collectionEntityClass, parentEntityClass);
        Field parentIdField = ProjectionHelperUtil.findIdField(parentEntityClass);
        Class<?> parentIdClass = parentIdField.getType();
        final Path<?> parentPath = root.get(parentRefField.getName());
        if (selections.stream().noneMatch(s -> s.getAlias().startsWith(parentRefField.getName() + "."))) {
            selections.add(parentPath.get(parentIdField.getName()).alias(parentRefField.getName() + "." + parentIdField.getName()));
        }

        cq.multiselect(selections);
        Predicate predicate = buildPredicate(parentIds, collectionEntityClass, parentEntityClass, root, em);
        cq.where(predicate);
        List<Tuple> childTuples = em.createQuery(cq).getResultList();
        List<Map<String, Object>> collectionEntities = ProjectionHelperUtil.tuplesToMap(childTuples);
        String parentRefFieldName = ProjectionHelperUtil.getParentReferenceField(collectionEntityClass, parentEntityClass);
        Map<Object, List<Map<String, Object>>> groupedCollections = groupByParentId(collectionEntities, parentRefFieldName, parentIds);
        mergeToParent(groupedCollections, parentEntitiesById, parentIds, collectionEntityField);

        Map<String, Field> collectionEntityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(collectionEntityClass);
        Collection<Field> collectionEntityFields = collectionEntityFieldsMap.values();

        Map<String, Field> collectionProjectFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(collectionProjectClass);
        Collection<Field> collectionProjectFields = collectionProjectFieldsMap.values();
        Field collectionIdField = ProjectionHelperUtil.findIdField(collectionEntityClass);
        String collectionIdFieldName = collectionIdField.getName();


        //从collection class里面递归子集合的集合
        for (Field projectField : collectionProjectFields) {
            Field collectionEntitySubField = collectionEntityFieldsMap.get(projectField.getName());
            if (collectionEntitySubField == null) continue;
            if (ProjectionHelperUtil.isAssociationOne(collectionEntitySubField)) {
                //递归关联字段下面有没有包含子集合
                Class<?> toOneEntityClass = collectionEntityFieldsMap.get(projectField.getName()).getType();
                Field toOneEntityIdField = ProjectionHelperUtil.findIdField(toOneEntityClass);
                String toOneEntityIdFieldName = toOneEntityIdField.getName();

                Map<Object, Map<String, Object>> toOneEntitiesById = new HashMap<>();

                parentEntitiesById.values().forEach(f -> {
                    toOneEntitiesById.put(f.get(toOneEntityIdFieldName), f);
                });
                projectToOneCollection(toOneEntitiesById, toOneEntityClass, projectField.getType(), em);
            }

            if (!ProjectionHelperUtil.isCollectionField(projectField.getType())) {
                continue;
            }
            ;
            String projectFieldName = projectField.getName();
            log.info("递归子集合 {} 父实体类 {} 父投影类 {}", String.join(".", prefix, collectionFieldName, projectField.getName()), collectionEntityClass.getName(), collectionProjectClass.getName());

            //递归获取集合
            Map<Object, Map<String, Object>> parentById = new HashMap<>();
            collectionEntities.forEach(f -> parentById.put(f.get(collectionIdFieldName), f));
            projectCollection(parentById, collectionEntityClass, collectionProjectClass, projectFieldName, em, String.join(".", prefix, collectionFieldName, projectField.getName()));
        }
    }


    private static void projectToOneCollection(Map<Object, Map<String, Object>> parentEntitiesById, Class<?> entityClass, Class<?> projectClass, EntityManager em) {
        Map<String, Field> entityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(entityClass);
        Map<String, Field> projectFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(projectClass);
        log.info("递归子集合的关联字段 {} {}", entityClass.getName(), projectClass.getName());
        for (Field projectField : projectFieldsMap.values()) {
            String projectFieldName = projectField.getName();
            Class<?> projectFieldClass = projectField.getType();
            Field entityField = entityFieldsMap.get(projectFieldName);
            if (entityField == null) continue;
            Class<?> entityFieldClass = entityField.getType();

            if (ProjectionHelperUtil.isAssociationOne(entityField)) {
                //递归关联字段下面有没有包含子集合
                //按关联的对象重新收集父类
                Map<Object, Map<String, Object>> toOneEntitiesById = new HashMap<>();
                Field toOneEntityIdField = ProjectionHelperUtil.findIdField(entityFieldClass);
                String toOneEntityIdFieldName = toOneEntityIdField.getName();
                parentEntitiesById.values().forEach(f -> {
                    toOneEntitiesById.put(f.get(toOneEntityIdFieldName), f);
                });
                projectToOneCollection(toOneEntitiesById, entityFieldClass, projectFieldClass, em);
            }

            if (ProjectionHelperUtil.isCollectionField(projectFieldClass)) {
                log.info("递归到一的字段 {} {}", projectField.getName(), projectClass.getName());
                Class<?> collectionEntityClass = ProjectionHelperUtil.getCollectionGenericClass(entityField);
                Class<?> collectionProjectClass = ProjectionHelperUtil.getCollectionGenericClass(projectField);
//                projectCollection(parentEntitiesById,entityClass,projectClass,projectFieldName, em);
            }
        }
    }

    //构建集合的查询条件
    private static Predicate buildPredicate(Collection<Object> parentIds, Class<?> entityClass, Class<?> parentClass, Root<?> root, EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        Field parentField = ProjectionHelperUtil.getReferenceParentField(entityClass, parentClass);
        Field parentIdField = ProjectionHelperUtil.findIdField(parentField.getType());
        log.info("构建查询条件 父id列表大小 {} 实体类 {} 父类 {} 实体类在父类的字段名 {} 父类id {}", parentIds.size(), entityClass.getName(), parentClass.getName(), parentField.getName(), parentIdField.getName());
        final Class<?> parentIdClass = parentIdField.getType();
        final Path<?> parentPath = root.get(parentField.getName());
        if (parentIdField.isAnnotationPresent(EmbeddedId.class)) {
            Path<?> idPath = parentPath.get(parentIdField.getName());
            List<Predicate> predicates = new ArrayList<>();

            for (Object searchId : parentIds) {
                //这个时候parentId还是一个map，尚未转为为parent id的对象
                Map<String, Object> parentId = (Map<String, Object>) searchId;
                List<Predicate> andPredicates = new ArrayList<>();

                parentId.forEach((k, v) -> {
                    andPredicates.add(cb.equal(idPath.get(k), v));
                });

                Predicate andGroup = cb.and(andPredicates.toArray(new Predicate[0]));
                predicates.add(andGroup);
            }
//            describePredicate(predicates);
            return cb.or(predicates.toArray(new Predicate[0]));

        } else {
            //不是embeddedId的话，就获取其id字段来匹配
            final Path<?> parentIdPath = parentPath.get(parentIdField.getName());
//            final Field parentIdField_ = ProjectionHelperUtil.findIdField(parentClass);
//            final String alias = String.join(".", parentIdField.getName(), parentIdField_.getName());
            log.info("parentIdPath {}", parentIdPath.getAlias());
            return parentIdPath.in(parentIds);
        }


    }

    private static void describePredicate(List<Predicate> predicates) {
        predicates.forEach(predicate -> {
            if (predicate.getExpressions() != null && !predicate.getExpressions().isEmpty()) {
                String sss = predicate.getOperator() + " " +
                        predicate.getExpressions().stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                log.info("条件 {}", sss);
                ;
            }
        });

    }

    private static List<Selection<?>> buildSelection(String prefix,
                                                     From<?, ?> from,
                                                     Class<?> domainClass,
                                                     Class<?> projectClass) {

        //基础字段和embedded字段
        Map<String, Field> domainFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(domainClass);
        Collection<Field> domainFields = domainFieldsMap.values();
        Collection<Field> projectFields = ProjectionHelperUtil.getProjectFieldsMap(projectClass).values();
        List<Selection<?>> selections = new ArrayList<>();

        for (Field projectField : projectFields) {
            Class<?> projectFieldClass = projectField.getType();
            if (ProjectionHelperUtil.isCollectionField(projectFieldClass)) continue;

            String projectFieldName = projectField.getName();
            Field domainField = domainFieldsMap.get(projectFieldName);
            if (domainField == null) continue;

            String domainFieldName = domainField.getName();
            String alias = prefix.isEmpty() ? domainFieldName : prefix + "." + domainFieldName;
            //embedded的字段，直接递归select，安全

            if (ProjectionHelperUtil.isJavaStandardType(domainField)) {
                selections.add(from.get(domainFieldName).alias(alias));
            } else {
                log.info("递归嵌入 {}", domainFieldName);
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(from, domainFieldName);
                Class<?> joinClass = domainField.getType();
                selections.addAll(buildSelection(alias, join, joinClass, projectFieldClass));
            }


        }
        return selections;
    }


    /**
     * 处理基础字段，按照当前实体的id来匹配，实际上这个直接selection获取到了
     *
     * @param domainList   当前实体列表，key为实体的id, value为实体的值
     * @param domainClass  当前实体的类
     * @param projectClass 当前实体的投影类
     */
    public static void projectPrimitive(Map<Object, Map<String, Object>> domainList, Class<?> domainClass, Class<?> projectClass) {

    }


    /**
     * 处理嵌入字段，按照当前实体的id来匹配，实际上这个直接selection获取到了，要递归
     *
     * @param domainList   当前实体列表，key为实体的id, value为实体的值
     * @param domainClass  当前实体的类
     * @param projectClass 当前实体的投影类
     */
    public static void projectEmbedded(Map<Object, Map<String, Object>> domainList, Class<?> domainClass, Class<?> projectClass) {

    }


    /**
     * 处理to one 的关联字段
     */
    public static void projectToOne(Map<Object, Map<String, Object>> domainList, Class<?> domainClass, Class<?> projectClass) {
        Collection<Field> toOneFields = new HashSet<>();
        //toOne字段要找到@JoinColumn的name属性，从而获取真正的字段
        for (Field toOneField : toOneFields) {

        }
    }


    /**
     * 处理集合字段
     */
    public static void projectToMany() {

    }

    public static void loadCollectionsV2(
            List<Map<String, Object>> parentMaps,
            Class<?> domainClass,
            Class<?> projectionClass,
            Field domainIdField,
            EntityManager em) {

        List<Object> parentIds = parentMaps.stream()
                .map(m -> m.get(domainIdField.getName()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Collection<Field> projectFields = ProjectionHelperUtil.getProjectFieldsMap(projectionClass).values();
        for (Field projField : projectFields) {
            Field domainField = ProjectionHelperUtil.getDomainField(domainClass, projField.getName());
            if (!ProjectionHelperUtil.isCollectionField(projField.getType())) {
                continue;
            }
            ;

            String collectionName = projField.getName();
            Field collectionDomainField = ProjectionHelperUtil.getDomainField(domainClass, collectionName);
            if (collectionDomainField == null) continue;
            log.info("处理集合字段 {}", collectionDomainField.getName());
            // 集合元素类型（如 OrderItem.class）
            Class<?> elementDomainType = ProjectionHelperUtil.getCollectionElementType(collectionDomainField);
            Class<?> elementProjectType = ProjectionHelperUtil.getCollectionGenericClass(projField);

            // 外键字段（假设用 ManyToOne 映射回父类）
            Field parentIdField = ProjectionHelperUtil.findIdField(domainClass);
            Field parentRefField = ProjectionHelperUtil.getReferenceParentField(elementDomainType, domainClass);

            if (parentIds.isEmpty()) continue;
            projectEntity(elementDomainType, elementProjectType, parentMaps, domainClass, collectionDomainField, em);
        }

    }


    /**
     * @param domainClass           父类中的集合字段的实际实体类
     * @param projectClass          实体类的投影类
     * @param parentList            父实体的列表，用来将集合回写到父类的实体列表
     * @param parentClass           父类的实体类
     * @param parentCollectionField 父类中集合类的字段，用来将集合写回到父类的哪个字段
     * @param em
     */
    public static void projectEntity(Class<?> domainClass,
                                     Class<?> projectClass,
                                     List<Map<String, Object>> parentList,
                                     Class<?> parentClass,
                                     Field parentCollectionField,
                                     EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        //基础字段和embedded字段
        Map<String, Field> domainFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(domainClass);
        Collection<Field> domainFields = domainFieldsMap.values();
        Collection<Field> projectFields = ProjectionHelperUtil.getProjectFieldsMap(projectClass).values();
        Field parentIdField = ProjectionHelperUtil.findIdField(parentClass);
        String parentIdFieldName = parentIdField.getName();
        Field parentRefField = ProjectionHelperUtil.getReferenceParentField(domainClass, parentClass);//关联父类在子集合的字段名

        Map<Object, Map<String, Object>> parentObjectById = new HashMap<>();
        parentList.forEach(p -> parentObjectById.put(p.get(parentIdFieldName), p));

        List<Object> parentIds = new ArrayList<>(parentObjectById.keySet());

        List<Selection<?>> selections = new ArrayList<>();

        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<?> root = query.from(domainClass);

        for (Field projectField : projectFields) {
            String projectFieldName = projectField.getName();
            Class<?> projectFieldClass = projectField.getType();
            Field domainField = domainFieldsMap.get(projectFieldName);
            if (domainField == null) continue;
            String domainFieldName = domainField.getName();
            Class<?> domainFieldClass = domainField.getType();

            if (!ProjectionHelperUtil.isValidField(projectField)) continue;

            //集合字段
            if (ProjectionHelperUtil.isCollectionField(domainFieldClass)) {

            }

            //标准字段
            if (ProjectionHelperUtil.isJavaStandardType(domainFieldClass)) {
                log.info("普通字段 {}", domainFieldName);
                selections.add(root.get(domainFieldName).alias(domainFieldName));
            }

            //embedded字段
            if (ProjectionHelperUtil.isEmbedded(domainField)) {
                //要递归
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(root, domainField.getName());
                selections.addAll(buildEmbeddedSelection(domainField.getName(), join, domainFieldClass, projectFieldClass));
            }

            //关联字段
            if (ProjectionHelperUtil.isAssociationOne(domainField)) {
                //要递归
                projectEntity(domainFieldClass, projectFieldClass, parentList, domainClass, projectField, em);
            }
        }

        //查询普通字段和嵌入字段，将值放进当前的entity
        List<Tuple> tuples = fetchSimpleEntity(parentIds, parentClass, domainClass, query, root, selections, em);
        List<Map<String, Object>> tuplesMap = ProjectionHelperUtil.tuplesToMap(tuples);
        //简单字段是一对一关系，所以按父id索引即可
//        Map<Object,Map<String, Object>> childByParentId = keyByParentId(tuplesMap,parentIdFieldName, parentIds);
//        mergeToParent(childByParentId,parentObjectById,parentIds,parentCollectionField);

        //分组
        String parentRefIdName = ProjectionHelperUtil.getParentReferenceField(domainClass, parentClass);
        Map<Object, List<Map<String, Object>>> groupedChild = groupByParentId(tuplesMap, parentRefIdName, parentIds);
        //将结果放回父集合中
        mergeToParent(groupedChild, parentObjectById, parentIds, parentCollectionField);

        //递归

        Field domainIdField = ProjectionHelperUtil.findIdField(domainClass);
        loadCollectionsV2(tuplesMap, domainClass, projectClass, domainIdField, em);

//        boolean hasCollectionField = Arrays.stream(projectClass.getDeclaredFields())
//                .anyMatch(f -> Collection.class.isAssignableFrom(f.getType()));
//
//        for(Field projectField: projectFields){
//            if(!ProjectionHelperUtil.isCollectionField(projectField.getType())) continue;
//            Field domainField = ProjectionHelperUtil.getDomainField(domainClass,projectField.getName());
//            Class<?> collectionFieldDomainClass = ProjectionHelperUtil.getCollectionGenericClass(domainField);
//            Class<?> collectionFieldProjectClass = ProjectionHelperUtil.getCollectionElementProjectionClass(projectField, projectClass);
//            Field collectionClassIdField = ProjectionHelperUtil.findIdField(collectionFieldDomainClass);
//            loadCollectionsV2(tuplesMap, collectionFieldDomainClass,  collectionFieldProjectClass,collectionClassIdField, em);
//        }

    }

    private static Map<Object, Map<String, Object>> keyByParentId(
            List<Map<String, Object>> childList,
            String parentIdFieldName,
            List<Object> parentIds
    ) {
        if (childList == null || childList.isEmpty() || parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1️⃣ 先构建一个 parentId -> child 的快速索引
        Map<Object, Map<String, Object>> lookup = new HashMap<>();
        for (Map<String, Object> child : childList) {
            if (child != null) {
                lookup.put(child.get(parentIdFieldName), child);
            }
        }

        // 2️⃣ 再按 parentIds 顺序构建结果，未匹配的设为 null
        Map<Object, Map<String, Object>> result = new LinkedHashMap<>();
        for (Object parentId : parentIds) {
            // 用 Objects.equals 以支持 Map 或其他复杂对象作 key
            Map<String, Object> matched = lookup.entrySet().stream()
                    .filter(e -> Objects.equals(e.getKey(), parentId))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            result.put(parentId, matched);
        }

        return result;
    }

    /**
     * @param childList    查询出来的集合的列表
     * @param parentIdPath 关联父类在子集合的字段的名字，也就是@ManyToOne的字段的名字
     * @param parentIds    父类的id集合，用来将子集合分组，这样后续回写父类的时候就能直接通过父类的id直接获取到对应的子集合
     * @return
     */
    private static Map<Object, List<Map<String, Object>>> groupByParentId(
            List<Map<String, Object>> childList,
            String parentIdPath, // 支持 JSONPath 语法，例如 "$.parent.id"
            Collection<Object> parentIds
    ) {
        if (childList == null || childList.isEmpty() || parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyMap();
        }
//        Map<Object, List<Map<String, Object>>> result = new HashMap<>();
//        parentIds.forEach(parentId -> {
//             result.computeIfAbsent(parentId, k -> new ArrayList<>());
//             if(parentId.equals(parentIdPath)) {
//                 result.get(parentId).add(new HashMap<>());
//             }
//
//        });

        // 1️⃣ 按childList实际值分组
        Map<Object, List<Map<String, Object>>> grouped = childList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        map -> {
                            try {
                                return readJsonPath(map,parentIdPath);
                            } catch (Exception e) {
                                return null;
                            }
                        },
                        Collectors.toList()
                ));

        // 2️⃣ 按 parentIds 顺序输出
        Map<Object, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (Object parentId : parentIds) {
            List<Map<String, Object>> matched = grouped.entrySet().stream()
                    .filter(e -> Objects.equals(JsonUtils.getMapper().convertValue(e.getKey(), Map.class), parentId))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(Collections.emptyList());
            result.put(parentId, matched);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static Object readJsonPath(Object data, String path) {
        if (data == null || path == null || path.isEmpty()) {
            return null;
        }

        String[] tokens = path.split("\\.");

        Object current = data;
        for (String token : tokens) {
            if (current == null) return null;

            // 支持 array 下标访问，比如 b[0]
            int idxStart = token.indexOf('[');
            if (idxStart != -1 && token.endsWith("]")) {
                String key = token.substring(0, idxStart);
                int index = Integer.parseInt(token.substring(idxStart + 1, token.length() - 1));

                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(key);
                }

                if (current instanceof List) {
                    List<?> list = (List<?>) current;
                    if (index >= 0 && index < list.size()) {
                        current = list.get(index);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(token);
                } else {
                    return null;
                }
            }
        }

        return current;
    }


    /**
     * @param groupedChild          已按父id分组的子集合
     * @param parentByIdMap         按父id索引的父实体
     * @param parentIds             父id列表
     * @param parentCollectionField 子集合在父类中的字段名，用于回写到父实体的哪个字段中
     */
    private static void mergeToParent(Map<Object, List<Map<String, Object>>> groupedChild, Map<Object, Map<String, Object>> parentByIdMap, Collection<Object> parentIds, Field parentCollectionField) {
        parentIds.forEach(parentId -> {
            final Map<String, Object> parent = parentByIdMap.get(parentId);
            parent.put(parentCollectionField.getName(), groupedChild.get(parentId));
        });
    }

    private static List<Tuple> fetchSimpleEntity(
            List<Object> parentIds,
            Class<?> parentClass,
            Class<?> domainClass,
            CriteriaQuery<Tuple> cq,
            Path<?> root,
            List<Selection<?>> selections,
            EntityManager em
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        // ✅ 在当前 query 中创建 root
//        Root<?> root = cq.from(domainClass);

        Field parentIdField = ProjectionHelperUtil.getReferenceParentField(domainClass, parentClass);
        log.info("获取子集合 父ID集合长度 {} , 父类 {}  ", parentIds.size(), parentIdField.getType().getName());

        Class<?> parentIdClass = parentIdField.getType();
        Path<?> parentPath = root.get(parentIdField.getName());

        if (parentIdClass.isAnnotationPresent(EmbeddedId.class)) {
            if (selections.stream().noneMatch(s -> s.getAlias().startsWith(parentIdField.getName() + "."))) {
                selections.add(parentPath.alias(parentIdField.getName()));
            }

            List<Predicate> predicates = new ArrayList<>();
            for (Object searchId : parentIds) {
                Map<String, Object> parentId = (Map<String, Object>) searchId;
                List<Predicate> andPredicates = new ArrayList<>();
                parentId.forEach((k, v) -> andPredicates.add(cb.equal(parentPath.get(k), v)));
                predicates.add(cb.and(andPredicates.toArray(new Predicate[0])));
            }

            cq.where(cb.or(predicates.toArray(new Predicate[0])));
        } else {
            Field parentIdField_ = ProjectionHelperUtil.findIdField(parentClass);
            Path<?> parentIdPath = parentPath.get(parentIdField_.getName());
            String alias = parentIdField.getName() + "." + parentIdField_.getName();

            if (selections.stream().noneMatch(s -> s.getAlias().equals(alias))) {
                selections.add(parentIdPath.alias(alias));
            }

            cq.where(parentIdPath.in(parentIds));
        }

        cq.multiselect(selections);
        cq.getRoots().forEach(r -> System.out.println("Root: " + r.getJavaType()));

        return em.createQuery(cq).getResultList();
    }

    private static List<Selection<?>> buildEmbeddedSelection(String prefix,
                                                             From<?, ?> from,
                                                             Class<?> domainClass,
                                                             Class<?> projectClass) {

        //基础字段和embedded字段
        Map<String, Field> domainFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(domainClass);
        Collection<Field> domainFields = domainFieldsMap.values();
        Collection<Field> projectFields = ProjectionHelperUtil.getProjectFieldsMap(projectClass).values();
        List<Selection<?>> selections = new ArrayList<>();

        for (Field projectField : projectFields) {
            String projectFieldName = projectField.getName();
            Class<?> projectFieldClass = projectField.getType();
            Field domainField = domainFieldsMap.get(projectFieldName);
            if (domainField == null) continue;
            String domainFieldName = domainField.getName();
            String alias = prefix.isEmpty() ? projectFieldName : prefix + "." + projectFieldName;
            //embedded的字段，直接递归select，安全

            if (ProjectionHelperUtil.isEmbeddedId(domainField)) {
                log.info("递归嵌入 {}", domainFieldName);
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(from, domainFieldName);
                Class<?> joinClass = domainField.getType();
                selections.addAll(buildEmbeddedSelection(alias, join, joinClass, projectFieldClass));
            } else {
                selections.add(from.get(domainFieldName).alias(alias));
            }


        }
        return selections;
    }

    /**
     * key为字段名，根为类名例如实体类为Order 里面有products集合字段，则根的KEY为Order,集合字段root.products
     *
     * @return
     */
    public static List<Selection<?>> buildSelections(
            String prefix,
            From<?, ?> from,
            CriteriaBuilder cb,
            Class<?> domainClass,
            Class<?> projectionClass) {

        List<Selection<?>> selections = new ArrayList<>();
        log.info("投影 {} ", projectionClass.getName());
        for (Field projField : projectionClass.getDeclaredFields()) {
            if (!ProjectionHelperUtil.isValidField(projField)) continue;
            if (ProjectionHelperUtil.isCollectionField(projField.getType())) continue;

            String projFieldName = projField.getName();
            Field domainField = ProjectionHelperUtil.getDomainField(domainClass, projFieldName);
            if (domainField == null) continue;

            String domainFieldName = domainField.getName();
            String alias = prefix.isEmpty() ? projFieldName : prefix + "." + projFieldName;


            if (ProjectionHelperUtil.isEmbedded(domainField)) {
                //embedded的字段，直接递归select，安全
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(from, domainFieldName);
                Class<?> joinClass = domainField.getType();
                log.info("递归展开Embedded {}", domainFieldName);
                selections.addAll(buildSelections(alias, join, cb, joinClass, projField.getType()));
            } else if (ProjectionHelperUtil.isAssociationOne(domainField)) {
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(from, domainFieldName);
                Class<?> joinClass = domainField.getType();
                log.info("递归展开关联类 {}", domainFieldName);
                selections.addAll(buildSelections(alias, join, cb, joinClass, projField.getType()));
            }

            // ---------------------------
            // 标量字段
            // ---------------------------
            else if (ProjectionHelperUtil.isJavaStandardType(domainField.getType())) {
                log.info("普通字段 {}", domainFieldName);
                selections.add(from.get(domainFieldName).alias(alias));
            } else {
                log.info("跳过不处理字段: {}", domainFieldName);
            }
        }

        return selections;
    }


    /**
     * 处理集合字段
     *
     * @param parentIds
     * @param domainClass
     * @param projectionClass
     * @param em
     */
    public static void loadCollections(
            List<Map<String, Object>> parentMaps,
            Class<?> domainClass,
            Class<?> projectionClass,
            Field domainIdField,
            EntityManager em) {

        List<Object> parentIds = parentMaps.stream()
                .map(m -> m.get(domainIdField.getName()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Collection<Field> projectFields = ProjectionHelperUtil.getProjectFieldsMap(projectionClass).values();
        for (Field projField : projectFields) {
            Field domainField = ProjectionHelperUtil.getDomainField(domainClass, projField.getName());
            if (!ProjectionHelperUtil.isCollectionField(projField.getType())) {
                //非集合字段，循环获取简单
                if (ProjectionHelperUtil.isJavaStandardType(domainField)) {
                    loadCollections(parentMaps.stream().map(m -> (Map<String, Object>) m.get(domainField.getName())).toList(), domainField.getType(), projField.getType(), ProjectionHelperUtil.findIdField(domainField.getType()), em);
                }
                continue;
            }
            ;

            String collectionName = projField.getName();
            Field collectionDomainField = ProjectionHelperUtil.getDomainField(domainClass, collectionName);
            if (collectionDomainField == null) continue;
            log.info("处理集合字段 {}", collectionDomainField.getName());
            // 集合元素类型（如 OrderItem.class）
            Class<?> elementDomainType = ProjectionHelperUtil.getCollectionElementType(collectionDomainField);
            Class<?> elementProjectType = ProjectionHelperUtil.getCollectionGenericClass(projField);

            // 外键字段（假设用 ManyToOne 映射回父类）
            Field parentIdField = ProjectionHelperUtil.findIdField(domainClass);
            Field parentRefField = ProjectionHelperUtil.getReferenceParentField(elementDomainType, domainClass);

            if (parentIds.isEmpty()) continue;

            // ---------------------------
            // Step 2: 查询子集合
            // ---------------------------
            List<Tuple> childTuples = fetchChildTuples(parentIds, domainClass, elementDomainType, elementProjectType, em);
            List<Map<String, Object>> results = ProjectionHelperUtil.tuplesToMap(childTuples);
            String parentIdName = parentIdField.getName();
            results.forEach(child -> {
                final Object parentId = ((Map<String, Object>) child.get(parentRefField.getName())).get(parentIdField.getName()); //这个时候已经是embedded class的类型了，
                parentMaps.stream().filter(f -> {
                    if (ProjectionHelperUtil.isJavaStandardType(parentIdField)) {
                        return f.get(parentIdName).equals(parentId);
                    } else {
                        return ProjectionHelperUtil.equals(f.get(parentIdName), parentId);
                    }

                }).findFirst().ifPresent(p -> {
                    final List<Map<String, Object>> list = (List<Map<String, Object>>) p.computeIfAbsent(collectionDomainField.getName(), k -> new ArrayList<>());
                    list.add(child);
                });
            });

            //递归处理子集合

            boolean hasCollectionField = Arrays.stream(elementDomainType.getDeclaredFields())
                    .anyMatch(f -> Collection.class.isAssignableFrom(f.getType()));

            if (hasCollectionField) {
                // ----------------------------
                // Step 3: 加载集合字段
                // ----------------------------
//                DynamicProjectionHelper.loadCollectionsV2(results, domainType(), projectionClass, em, idField());
                final Field elementIdField = ProjectionHelperUtil.findIdField(elementDomainType);
                ProjectionHelperV2.loadCollections(results, elementDomainType, elementProjectType, elementIdField, em);
            }

        }

    }


    /**
     * @param parentIds    关联父类的id列表
     * @param parentClass  关联的父类
     * @param domainClass  子集合的实体类
     * @param projectClass 子集合实体类的投影类
     * @param em
     * @return
     */
    private static List<Tuple> fetchChildTuples(
            List<Object> parentIds,
            Class<?> parentClass,
            Class<?> domainClass,
            Class<?> projectClass,
            EntityManager em) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<?> root = cq.from(domainClass);
        // 选择子类所有非集合字段
        List<Selection<?>> selections = buildSelections("", root, cb, domainClass, projectClass);
        Field parentIdField = ProjectionHelperUtil.getReferenceParentField(domainClass, parentClass);
        log.info("获取子集合  父ID集合长度 {} , 父类 {}  ", parentIds.size(), parentIdField.getType().getName());
        final Class<?> parentIdClass = parentIdField.getType();
        final Path<?> parentPath = root.get(parentIdField.getName());
        if (parentIdClass.isAnnotationPresent(EmbeddedId.class)) {
            if (selections.stream().noneMatch(s -> s.getAlias().startsWith(parentIdField.getName() + "."))) {
                selections.add(parentPath.alias(parentIdField.getName()));
            }
            List<Predicate> predicates = new ArrayList<>();

            for (Object searchId : parentIds) {
                //这个时候parentId还是一个map，尚未转为为parent id的对象
                Map<String, Object> parentId = (Map<String, Object>) searchId;
                List<Predicate> andPredicates = new ArrayList<>();

                parentId.forEach((k, v) -> {
                    andPredicates.add(cb.equal(parentPath.get(k), v));
                });

                Predicate andGroup = cb.and(andPredicates.toArray(new Predicate[0]));
                predicates.add(andGroup);
            }

            cq.where(cb.or(predicates.toArray(new Predicate[0])));

        } else {
            //不是embeddedId的话，就获取其id字段来匹配
            final Path<?> parentIdPath = parentPath.get(ProjectionHelperUtil.findIdField(parentClass).getName());

            final Field parentIdField_ = ProjectionHelperUtil.findIdField(parentClass);
            final String alias = String.join(".", parentIdField.getName(), parentIdField_.getName());

            if (selections.stream().noneMatch(s -> s.getAlias().equals(alias))) {
                selections.add(parentIdPath.alias(alias));
            }
            cq.where(parentIdPath.in(parentIds));
        }

        cq.multiselect(selections);
        return em.createQuery(cq).getResultList();
    }

}
