package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ProjectionHelper {

    /**
     * 所有非集合字段都可以一次性全部获取出来
     */
    protected static List<Selection<?>> buildEntitySelection(Root<?> root, Class<?> entityClass, Class<?> projectClass) {
        Map<String, Field> entityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(entityClass);
        Map<String, Field> projectFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(projectClass);
        Collection<Field> entityFields = entityFieldsMap.values();
        Collection<Field> projectFields = projectFieldsMap.values();
        List<Selection<?>> selections = new ArrayList<>();

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
                log.debug("递归嵌入字段 {}.{} -> {}",projectClass.getName() , projectField.getName(), entityFieldClass.getName());
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
                log.debug("递归关联字段 {}.{} -> {}", projectClass.getName(),projectField.getName(),entityFieldClass.getName());
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
    private static List<Tuple> projectEntity(Collection<Object> parentIds, Class<?> parentEntityClass, Class<?> entityClass, Class<?> projectClass, EntityManager em,Field fieldInParent) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<?> root = cq.from(entityClass);


        //获取子集合类的所有非集合的投影字段
        List<Selection<?>> selections = buildEntitySelection(root, entityClass, projectClass);
        Field parentRefField = ProjectionHelperUtil.getReferenceParentField(entityClass, parentEntityClass);
        List<Order> orders = buildOrderByFromAnnotation(root,cb,fieldInParent);
        Field parentIdField = ProjectionHelperUtil.findIdField(parentEntityClass);
        Class<?> parentIdClass = parentIdField.getType();
        final Path<?> parentPath = root.get(parentRefField.getName());
        if (selections.stream().noneMatch(s -> s.getAlias().startsWith(parentRefField.getName() + "."))) {
            selections.add(parentPath.get(parentIdField.getName()).alias(parentRefField.getName() + "." + parentIdField.getName()));
        }
        cq.multiselect(selections);
        Predicate predicate = buildPredicate(parentIds, entityClass, parentEntityClass, root, em);
        cq.where(predicate);
        cq.orderBy(orders);
        return em.createQuery(cq).getResultList();
    }

    /**
     * 根据实体字段上的 @OrderBy 注解生成 CriteriaQuery 排序
     *
     * @param root              Root 对象
     * @param cb                CriteriaBuilder
     * @param field         集合字段
     * @return List<Order> 可直接传给 cq.orderBy(...)
     */
    public static List<Order> buildOrderByFromAnnotation(

            Root<?> root,
            CriteriaBuilder cb,
            Field field
    ) {
        List<Order> orders = new ArrayList<>();

        //            Field field = entityClass.getDeclaredField(fieldName);
        OrderBy orderByAnno = field.getAnnotation(OrderBy.class);
        if (orderByAnno == null) {
            return orders; // 没有 @OrderBy 注解
        }

        String value = orderByAnno.value(); // e.g. "seq ASC, createdAt DESC"
        if (value == null || value.trim().isEmpty()) {
            return orders;
        }

        String[] orderParts = value.split(",");
        for (String part : orderParts) {
            part = part.trim();
            String[] tokens = part.split("\\s+");
            String propertyPath = tokens[0]; // 支持嵌套属性 "parent.name"
            boolean asc = tokens.length == 1 || "ASC".equalsIgnoreCase(tokens[1]);

            // 解析嵌套路径
            Path<?> path = root;
            for (String p : propertyPath.split("\\.")) {
                path = path.get(p);
            }

            orders.add(asc ? cb.asc(path) : cb.desc(path));
        }

        return orders;
    }

    protected static Map<Object, Map<String, Object>> projectCollectionV2(final Map<Object, Map<String, Object>> parentEntitiesByIdMap, Class<?> parentEntityClass, Class<?> entityClass, Class<?> projectClass, EntityManager em,final String fieldNameInParent) {
        //根据父id，先获取到自己的所有非集合对象
        Map<String, Field> entityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(entityClass);
        Map<String, Field> projectFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(projectClass);
        Map<String, Field> parentEntityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(parentEntityClass);

        Collection<Field> entityFields = entityFieldsMap.values();
        Collection<Field> projectFields = projectFieldsMap.values();
        final Collection<Object> parentIds = parentEntitiesByIdMap.keySet();
        Map<Object, Map<String, Object>> result = Map.of();

        Class<?> collectionFieldTypeInParent= ProjectionHelperUtil.getCollectionGenericClass(parentEntityFieldsMap.get(fieldNameInParent));
        boolean isCollectionFieldOfParent = !collectionFieldTypeInParent.isAnnotationPresent(Embeddable.class) && ProjectionHelperUtil.isCollectionField(parentEntityFieldsMap.get(fieldNameInParent).getType());
        //获取到自己的id集合
        Map<Object, Map<String, Object>> childEntitiesByIdMap = new HashMap<>();

        if (isCollectionFieldOfParent) {
            Field collectionFieldInParent = parentEntityFieldsMap.get(fieldNameInParent);

            //只有自己是父的集合的时候，才需要到数据库根据父id，查询出来project，
            List<Tuple> childTuples = projectEntity(parentIds, parentEntityClass, entityClass, projectClass, em,collectionFieldInParent);

            //获取排序
            log.debug("查询结果 {}", childTuples.size());
            List<Map<String, Object>> collectionEntities = ProjectionHelperUtil.tuplesToMap(childTuples);
            childEntitiesByIdMap = keyById(collectionEntities, entityClass);
            String parentRefFieldName = ProjectionHelperUtil.getParentReferenceField(entityClass, parentEntityClass);
            final Map<Object, List<Map<String, Object>>> groupedCollections = groupByParentId(collectionEntities, parentRefFieldName, parentIds);

            //放回大树
            mergeToParent(groupedCollections, parentEntitiesByIdMap, parentIds, parentEntityFieldsMap.get(fieldNameInParent));

            try {
                log.debug("回写后情况 {} ",JsonUtils.getMapper().writeValueAsString(parentEntitiesByIdMap));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {

            /**如果不是集合，则在父类中已经通过join查询出来了，直接获取他的列表值即可。这里注意，如果关联的实体所在的实体是复合主键，则需要用所在实体的主键作为索引
             * 例如以下MemberInfoDtoForVote
             * @Value
             * public class AttendTouPiaoDtoSimple implements Serializable {
             *     AttendTouPiao.AttendTouPiaoId id;
             *
             *     MemberInfoDtoForVote memberInfo;
             * }
             * 如果用自己的主键作为索引，则会丢失部分实体
             * 因为关联实体，传入的索引里面的key，就为关联实体所在的实体的id
             */

            List<Map<String, Object>> childEntities = getAllByKey(parentEntitiesByIdMap.values(), fieldNameInParent);
            childEntitiesByIdMap =  keyById(childEntities, entityClass);
            result = childEntitiesByIdMap;
//            for(Object key : parentEntitiesByIdMap.keySet()){
//                childEntitiesByIdMap.put(key, parentEntitiesByIdMap.get(fieldNameInParent));
//            }
            try {
                log.debug("关联字段汇总 {} {}",fieldNameInParent, JsonUtils.getMapper().writeValueAsString(childEntitiesByIdMap));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        //如果是@JsonIgonore或者jsonBackRefernce，就不再递归
        Field fieldInParent = parentEntityFieldsMap.get(fieldNameInParent);
        if(fieldInParent.isAnnotationPresent(JsonIgnore.class)||fieldInParent.isAnnotationPresent(JsonBackReference.class)){
            //返回集合
            return result;
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
                try {
                    log.debug("递归子集合 {}.{} {}", projectClass.getName(), projectFieldName, JsonUtils.getMapper().writeValueAsString(childEntitiesByIdMap));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                projectCollectionV2(childEntitiesByIdMap, entityClass, collectionEntityClass, collectionProjectClass, em, projectFieldName);
//                final Map<Object, List<Map<String, Object>>> result_ = projectCollectionV2(childEntitiesByIdMap, entityClass, collectionEntityClass, collectionProjectClass, em, projectFieldName);
                try {
                    log.debug("递归子集合后的结果 {}.{} {}", projectClass.getName(), projectFieldName, JsonUtils.getMapper().writeValueAsString(childEntitiesByIdMap));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else if (ProjectionHelperUtil.isAssociationOne(entityField)) {
                try {
                    log.debug("递归关联实体 {}.{} {}", projectClass.getName(), projectFieldName, JsonUtils.getMapper().writeValueAsString(childEntitiesByIdMap));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
//                projectCollectionV2(childEntitiesByIdMap, entityClass, entityField.getType(), projectFieldClass, em, projectFieldName);
               final Map<Object, Map<String, Object>> result_ =  projectCollectionV2(childEntitiesByIdMap, entityClass, entityField.getType(), projectFieldClass, em, projectFieldName);
                /**
                 * 将关联实体返回的结果，合并到当前对象中，这个时候需要知道这个关联对象对应的join column值，从而确定根据哪个id来查询替换
                 */
                Field toOnEntityIdField = ProjectionHelperUtil.findIdField(entityField.getType());
                childEntitiesByIdMap.values().stream().filter(f->f.containsKey(projectFieldName)).forEach(child->{

                    Object id = ((Map<String,Object>)child.get(projectFieldName)).get(toOnEntityIdField.getName());
                    child.put(projectFieldName, result_.get(id));
                });
                result = childEntitiesByIdMap;
                try {
                    log.debug("递归关联实体后返回的结果 {}.{} {}", projectClass.getName(), projectFieldName, JsonUtils.getMapper().writeValueAsString(result_));
                    log.debug("递归关联实体后的结果 {}.{} {}", projectClass.getName(), projectFieldName, JsonUtils.getMapper().writeValueAsString(childEntitiesByIdMap));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        //返回集合
        return result;
    }

    protected  static Map<Object, Map<String, Object>> keyById(List<Map<String, Object>> entityList, Class<?> entityClass) {
        Field idField = ProjectionHelperUtil.findIdField(entityClass);
        Map<Object, Map<String, Object>> map = new HashMap<>();
        entityList.forEach(f -> {
            Object id = f.get(idField.getName());
            map.put(id, f);
        });
        return map;
    }

    protected static List<Map<String, Object>> getAllByKey(Collection<Map<String, Object>> entityList, String fieldName) {
        return entityList.stream().map(entity -> (Map<String, Object>) entity.get(fieldName)).filter(Objects::nonNull).toList();
    }


    //构建集合的查询条件
    private static Predicate buildPredicate(final Collection<Object> parentIds, Class<?> entityClass, Class<?> parentClass, Root<?> root, EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        Field parentField = ProjectionHelperUtil.getReferenceParentField(entityClass, parentClass);
        Field parentIdField = ProjectionHelperUtil.findIdField(parentField.getType());
        log.debug("构建查询条件 {}.{}.{}({}) in 大小{} ", entityClass.getName(), parentField.getName(), parentIdField.getName(), parentClass.getName(),parentIds.size());
//        if(String.join(".",entityClass.getName(), parentField.getName(), parentIdField.getName()).equals("cn.linkairtech.toupiao.model.MemberTeam.memberInfo.username")){
//            parentIds.forEach(p->{
//                System.out.print("'"+p + "',");
//            });
//        }

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
            log.debug("复合主键");
            return cb.or(predicates.toArray(new Predicate[0]));

        } else {
            //不是embeddedId的话，就获取其id字段来匹配
            final Path<?> parentIdPath = parentPath.get(parentIdField.getName());
//            final Field parentIdField_ = ProjectionHelperUtil.findIdField(parentClass);
//            final String alias = String.join(".", parentIdField.getName(), parentIdField_.getName());
            log.debug("parentIdPath {}", parentIdPath.getAlias());
            return parentIdPath.in(parentIds);
        }


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
                log.debug("递归嵌入 {}", domainFieldName);
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(from, domainFieldName);
                Class<?> joinClass = domainField.getType();
                selections.addAll(buildSelection(alias, join, joinClass, projectFieldClass));
            }


        }
        return selections;
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
                    .filter(e -> {
                        if(ProjectionHelperUtil.isJavaStandardType(e.getKey().getClass())){
                            return Objects.equals(e.getKey(),parentId);
                        }else{
                            return Objects.equals(JsonUtils.getMapper().convertValue(e.getKey(), Map.class), parentId);
                        }

                    })
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(Collections.emptyList());
            result.put(parentId, matched);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object readJsonPath(Object data, String path) {
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
    private static void mergeToParent(final Map<Object, List<Map<String, Object>>> groupedChild,final Map<Object, Map<String, Object>> parentByIdMap,final Collection<Object> parentIds,final Field parentCollectionField) {
//        log.debug("回写到 {} 主ast树 {}",parentByIdMap, parentCollectionField.getName());
        parentIds.forEach(parentId -> {
            final Map<String, Object> parent = parentByIdMap.get(parentId);
            if(parent!=null){
//                log.debug("回写字段 {} 对象 {} 集合长度 {}", parentCollectionField.getName(),parentId, groupedChild.get(parentId).size());
                parent.put(parentCollectionField.getName(), groupedChild.get(parentId));
            }else{
                log.debug("没有找到回写父对象 {}", parentId);
            }

        });
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
                log.debug("递归嵌入 {}", domainFieldName);
                Join<?, ?> join = ProjectionHelperUtil.tryGetOrCreateJoin(from, domainFieldName);
                Class<?> joinClass = domainField.getType();
                selections.addAll(buildEmbeddedSelection(alias, join, joinClass, projectFieldClass));
            } else {
                selections.add(from.get(domainFieldName).alias(alias));
            }


        }
        return selections;
    }


}
