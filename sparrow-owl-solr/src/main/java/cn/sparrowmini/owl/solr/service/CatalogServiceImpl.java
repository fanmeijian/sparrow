package cn.sparrowmini.owl.solr.service;

import cn.sparrowmini.common.dto.*;
import cn.sparrowmini.common.service.CatalogService;
import cn.sparrowmini.common.util.JsonUtils;
import cn.sparrowmini.owl.solr.model.ConceptType;
import cn.sparrowmini.owl.solr.model.Restriction;
import cn.sparrowmini.owl.solr.model.SkosRestriction;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.apache.jena.vocabulary.SKOS;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.request.json.QueryFacetMap;
import org.apache.solr.client.solrj.request.json.TermsFacetMap;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CatalogServiceImpl implements CatalogService {
    private final SolrClient solrClient;
    private final String ns = "http://www.cn-plc.com/ontology/cms#";

    @Override
    public List<OwlClass> getAllClasses() {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.setFields("localName", "zh_label", "id","children","properties","allParents");
        solrQuery.setRows(Integer.MAX_VALUE);
        try {
            QueryResponse response = solrClient.query("class", solrQuery);
            return response.getResults().stream().map(doc ->
                    OwlClass.builder()
                            .id(doc.get("id").toString())
                            .name(getObjectString(doc.get("localName")))
                            .label(getObjectString(doc.get("zh_label")))
                            .children(getObjectStringArray(doc.getFieldValues("children")))
                            .properties(getObjectStringArray(doc.getFieldValues("properties")))
                            .allParents(getObjectStringArray(doc.getFieldValues("allParents")))
                            .build()
            ).collect(Collectors.toList());
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<OwlProperty> getAllProperties() {
        SolrQuery solrQuery = new SolrQuery("doctype:property");
        solrQuery.setFields("localName", "zh_label", "id","children","propType","range");
        solrQuery.setRows(Integer.MAX_VALUE);
        try {
            QueryResponse response = solrClient.query("props", solrQuery);
            return response.getResults().stream().map(doc ->
                    OwlProperty.builder()
                            .id(doc.get("id").toString())
                            .name(getObjectString(doc.get("localName")))
                            .label(getObjectString(doc.get("zh_label")))
                            .children(getObjectStringArray(doc.getFieldValues("children")))
                            .range(getObjectStringArray(doc.getFieldValues("range")))
                            .propType(getObjectString(doc.get("propType")))
                            .build()
            ).collect(Collectors.toList());
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<RestrictionDto> getAllRestrictions() {
        SolrQuery solrQuery = new SolrQuery("doctype:restriction");
//        solrQuery.setFields("localName", "zh_label", "id","children","propType","range");
        solrQuery.setRows(Integer.MAX_VALUE);
        try {
            QueryResponse response = solrClient.query("props", solrQuery);
            return response.getResults().stream().map(doc ->
                    RestrictionDto.builder()
                            .id(doc.get("id").toString())
                            .value(getObjectStringArray(doc.getFieldValues("value")))
                            .isRequired((Boolean) doc.get("isRequired"))
                            .onProperty(getObjectString(doc.get("onProperty")))
                            .onClass(getObjectString(doc.get("onClass")))
                            .valueProperty(getObjectString(doc.get("valueProperty")))
                            .build()
            ).collect(Collectors.toList());
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ConceptTypeDto> getAllConcepts() {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.setFields("localName", "zh_label", "id","children","properties");
        solrQuery.setRows(Integer.MAX_VALUE);
        try {
            QueryResponse response = solrClient.query("concepts", solrQuery);
            return response.getResults().stream().map(doc ->
                    ConceptTypeDto.builder()
                            .id(doc.get("id").toString())
                            .name(getObjectString(doc.get("localName")))
                            .label(getObjectString(doc.get("zh_label")))
                            .topConceptOf(getObjectStringArray(doc.getFieldValues("topConceptOf")))
                            .inScheme(getObjectStringArray(doc.getFieldValues("inScheme")))
                            .memberOf(getObjectStringArray(doc.getFieldValues("memberOf")))
                            .build()
            ).collect(Collectors.toList());
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public List<ItemVo> getChildrenByClassId(String parentId) {
        String parentUri = getUri(parentId);
        String query;

        if (parentId == null) {
            query = "doctype:class AND -parents:[* TO *]";
        } else {
            // 💡 关键修改：调换 from 和 to 的位置！
            // from=parents (从子节点的多值父引脚出发) -> to=id (对齐到父节点的唯一标示)
            query = String.format("{!graph from=parents to=id returnRoot=false}id:\"%s\"", parentUri);
        }

        Set<String> fields = Set.of("id", "parents", "localName", "zh_label");
        SolrQuery queryChild = new SolrQuery(query);
        queryChild.setFields(fields.toArray(new String[0]));
        queryChild.setRows(5000);

        try {
            QueryResponse response = solrClient.query("class", queryChild);
            List<SolrDocument> flatResults = response.getResults();

            // 【调试日志】请务必观察控制台输出！
            System.out.println("====== Solr 原始返回数量: " + flatResults.size() + " ======");

            // 1. 初始化：生成全局唯一的 ID -> ItemVo 映射
            Map<String, ItemVo> allVoMap = new HashMap<>();
            for (SolrDocument doc : flatResults) {
                String id = getObjectString(doc.get("id"));
                ItemVo vo = ItemVo.builder()
                        .name(getObjectString(doc.get("localName")))
                        .label(getObjectString(doc.get("zh_label")))
                        .children(new ArrayList<>())
                        .build();
                allVoMap.put(id, vo);
            }

            // 存放最终返回给前端的顶层子分类
            List<ItemVo> rootNodes = new ArrayList<>();

            // 2. 核心编织
            for (SolrDocument doc : flatResults) {
                String currentId = getObjectString(doc.get("id"));
                ItemVo currentVo = allVoMap.get(currentId);

                // 提取多值 parents
                List<String> pUris = new ArrayList<>();
                Object parentsObj = doc.get("parents");
                if (parentsObj instanceof Collection<?>) {
                    ((Collection<?>) parentsObj).forEach(o -> pUris.add(getObjectString(o)));
                } else if (parentsObj != null) {
                    pUris.add(getObjectString(parentsObj));
                }

                // A. 如果是查全盘系统的根节点
                if (parentId == null && pUris.isEmpty()) {
                    rootNodes.add(currentVo);
                    continue;
                }

                // B. 如果是查某个节点下的子孙
                boolean isFirstLevel = false;
                for (String pUri : pUris) {
                    // 核心判定 1：只要当前节点的父级列表中包含了我们正在检索的 parentUri，它就是第一层子分类
                    if (parentId != null && parentUri.equals(pUri)) {
                        isFirstLevel = true;
                    }

                    // 核心判定 2：无论是不是第一层，只要它的父亲在本次结果集里，就建立内存父子连线（供孙分类挂载）
                    ItemVo parentVo = allVoMap.get(pUri);
                    if (parentVo != null) {
                        if (!parentVo.getChildren().contains(currentVo)) {
                            parentVo.getChildren().add(currentVo);
                        }
                    }
                }

                // 如果被确认为第一层子分类，塞入返回结果中
                if (isFirstLevel && !rootNodes.contains(currentVo)) {
                    rootNodes.add(currentVo);
                }
            }

            // 3. 计算 childCount
            allVoMap.values().forEach(vo -> {
                if (vo.getChildren() != null) {
                    vo.setChildCount((long) vo.getChildren().size());
                }
            });

            System.out.println("====== 最终组装出的第一层数量: " + rootNodes.size() + " ======");
            return rootNodes;

        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public List<ItemVo> getChildrenByClassId(String parentId) {
//        String parentUri = getUri(parentId);
//        String query = "*:*";
//        if (parentId == null) {
//            query = "doctype:class AND -parents:[* TO *]";
//        } else {
//            query = "doctype:class AND parents:\"" + parentUri + "\"";
//        }
//        Set<String> fields = Set.of("localName", "zh_label");
//        SolrQuery queryChild = new SolrQuery(query);
//        queryChild.setFields(fields.toArray(new String[0]));
//        queryChild.setRows(1000);
//        try {
//            QueryResponse response = solrClient.query("class", queryChild);
//            return response.getResults().stream().map(doc ->
//                    ItemVo.builder()
//                            .name(getObjectString(doc.get("localName")))
//                            .label(getObjectString(doc.get("zh_label")))
//                            .build()
//            ).toList();
//        } catch (SolrServerException | IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private String getObjectString(Object object) {
        return object == null ? "" : object.toString();
    }

    private List<String> getObjectStringArray(Collection<Object> object) {
        return object == null ? List.of() : object.stream().map(Objects::toString).toList();
    }

    @Override
    public List<PropertyVo> getPropertiesByClassId(String catalogId) {
        String currentClassUri = getUri(catalogId);

        // =========================================================================
        // 🌟 核心变化 1：并发捞取属于当前类的限制规则 (doctype:restriction)
        // =========================================================================
        Map<String, Restriction> restrictionMap = new HashMap<>();
        SolrQuery restrictionQuery = new SolrQuery("doctype:restriction AND onClass:\"" + currentClassUri + "\"");
        restrictionQuery.setRows(500); // 类的重写规则一般也就十几个

        try {
            // 假设你的 restriction 文档和 concepts 存放在同一个核心（或指定核心）
            QueryResponse resResponse = solrClient.query("props", restrictionQuery);
            resResponse.getResults()
                    .forEach(doc -> {
                        if (doc.getFieldValueMap() != null) {
                            restrictionMap.put(doc.get("onProperty").toString(), JsonUtils.getMapper().convertValue(doc, SkosRestriction.class));

                        }
                    });
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException("捞取类属性限制失败", e);
        }

        // 1. 匹配直接用于当前分类的属性
        SolrQuery query = new SolrQuery("used_in:\"" + currentClassUri + "\"");
        query.setRows(1000); // 属性通常不会上千，一次性全部捞回
        try {
            // 🚀 核心变化：直接去查 "props" 集合，一步到位拿到带完整元数据的 Property
            QueryResponse response = solrClient.query("props", query);
            return response.getResults().stream().map(doc -> {
                String propertyUri = doc.get("id").toString();
                Restriction restriction = restrictionMap.get(propertyUri);
                String activeBroader = restriction instanceof SkosRestriction ? ((SkosRestriction) restriction).getBroader() : null;
                String scheme = restriction instanceof SkosRestriction ? ((SkosRestriction) restriction).getScheme() : null;
                return PropertyVo.builder()
                        .name(doc.get("localName").toString())
                        .label(getObjectString(doc.get("zh_label")))
                        .type(doc.get("propType").toString())
                        .isFacet((boolean) doc.get("isFacet"))
                        .isRequired(restriction != null ? restriction.getIsRequired() : false)
                        .isVisible((boolean) doc.get("isVisible"))
                        .ranges((List<String>) doc.get("range"))
                        .restriction(JsonUtils.getMapper().convertValue(restriction, RestrictionDto.class))
                        .build();
            }).collect(Collectors.toList());
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ItemVo> getCodeTypeByCodeListId(String codeListId) {
        String parentUri = getUri(codeListId);
        String query = "codedList:\"" + parentUri + "\"";
        Set<String> fields = Set.of("localName", "zh_label");
        SolrQuery queryChild = new SolrQuery(query);
        queryChild.setFields(fields.toArray(new String[0]));
        queryChild.setRows(1000);
        try {
            QueryResponse response = solrClient.query("codes", queryChild);
            return response.getResults().stream()
                    .map(doc ->
                            ItemVo.builder().name(doc.get("localName").toString())
                                    .label(doc.get("zh_label").toString())
                                    .build())
                    .toList();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAllChildrenIdByParentClassId(String parentId) {
        try {
            SolrDocument solrDocument = solrClient.getById("class", getUri(parentId));
            if (solrDocument != null) {
                return (List<String>) solrDocument.getFieldValue("allChildren");
            } else {
                return List.of();
            }

        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUri(String name) {
        return name == null || name.contains(ns) ? name : ns + name;
    }

    public List<ConceptType> getOptionsByCodeListId(String codeListId) throws SolrServerException, IOException {
        // 1. 先去 Solr 里查一下这个 codeListId 本身是个什么 doctype
        SolrDocument doc = solrClient.getById("concepts", codeListId);
        String doctype = (String) doc.getFieldValue("doctype");

        // 2. 智能化自动变阵（对前端完全隐蔽）
        SolrQuery query = new SolrQuery();
        if ("scheme".equals(doctype)) {
            // 如果是 Scheme，捞取该沙盒下的顶级根节点
            query.setQuery("skos_inScheme:\"" + codeListId + "\" AND isTopConcept:true");
        } else if ("collection".equals(doctype)) {
            // 如果是 Collection，顺着你洗好的递归标签，横向打包捞出所有成员
            query.setQuery("skos_memberOf:\"" + codeListId + "\"");
        }

        return solrClient.query("concepts", query).getBeans(ConceptType.class);
    }

    @Override
    public List<ItemVo> getOptionsByProperty(String property, String restrictionId) {
        try {
            SolrDocument propertyDoc = solrClient.getById("props", getUri(property));
            Collection<Object> ranges = propertyDoc.getFieldValues("range");
            if(ranges == null || ranges.isEmpty()) {
                return List.of();
            }
            if (ranges.stream().anyMatch(r -> r.equals(SKOS.Concept.getURI()))) {
                if (!isNullString(restrictionId)) {
                    SolrDocument restrictionDoc = solrClient.getById("props", restrictionId);

                    String valueProperty = (String) restrictionDoc.getFieldValue("valueProperty");
                    Collection<Object> values = restrictionDoc.getFieldValues("value");
                    String value = values.stream().findFirst().orElse("").toString();

                    if (valueProperty.equals(SKOS.broader.getURI())) {
                        return this.getConceptsByScheme(null, value);
                    } else if (valueProperty.equals(SKOS.member.getURI())) {
                        return this.getConceptsByCollection(value);
                    } else if (valueProperty.equals(SKOS.topConceptOf.getURI())) {
                        return this.getConceptsByScheme(value, null);
                    }

                }
            } else {
                Object range = ranges.stream().findFirst().orElse(null);
                if (range instanceof String r) {
                    return this.getChildrenByClassId(r);
                }


            }

        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }

        return List.of();
    }

    private List<ItemVo> getConceptsByScheme(String scheme, String broader) {
        // 1. 先去 Solr 里查一下这个 codeListId 本身是个什么 doctype
        String queryStr = "";

        if (!isNullString(scheme) && !isNullString(broader)) {
            queryStr = "inScheme:\"" + scheme + "\" AND broader:\"" + broader + "\"";
        } else if (!isNullString(scheme)) {
            queryStr = "inScheme:\"" + scheme + "\" AND topConceptOf:\"" + scheme + "\"";
        } else if (!isNullString(broader)) {
            queryStr += "broader:\"" + broader + "\"";
        }

        // 2. 智能化自动变阵（对前端完全隐蔽）
        SolrQuery query = new SolrQuery(queryStr);
        query.setRows(1000);
        query.setFields("localName", "zh_label");

        try {
            return solrClient.query("concepts", query).getBeans(ConceptType.class)
                    .stream().map(m -> ItemVo.builder().label(m.getLabel().get("zh_label")).name(m.getLocalName()).build()).toList();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    private List<ItemVo> getConceptsByCollection(String collection) {
        // 自动补全可能缺失的命名空间前缀
        String collectionUri = getUri(collection);

        // 💡 关键修正 1：图查询路径。起点是当前 collection。
        // 游走逻辑：下一层节点的 memberOf (或 parentRefs) 会指向上一层节点的 id
        String graphQuery = String.format("{!graph from=memberOf to=id returnRoot=false}id:\"%s\"", collectionUri);

        SolrQuery query = new SolrQuery(graphQuery);
        query.setRows(5000); // 确保能一次性装下所有子孙节点
        // 必须把用于拉线织网的 id, memberOf, parentRefs 全部查出来
        query.setFields("id", "localName", "zh_label", "memberOf", "parentRefs");

        try {
            QueryResponse queryResponse = solrClient.query("concepts", query);
            List<SolrDocument> flatResults = queryResponse.getResults();

            System.out.println("====== [Concepts] Solr 原始返回数量: " + flatResults.size() + " ======");

            if (flatResults.isEmpty()) {
                return new ArrayList<>();
            }

            // 步骤 1：第一遍遍历，将所有文档实例化为孤立的 ItemVo 存入 Map
            Map<String, ItemVo> allVoMap = new HashMap<>();
            for (SolrDocument doc : flatResults) {
                String id = getObjectString(doc.get("id"));
                ItemVo vo = ItemVo.builder()
                        .name(getObjectString(doc.get("localName")))
                        .label(getObjectString(doc.get("zh_label")))
                        .children(new ArrayList<>())
                        .build();
                allVoMap.put(id, vo);
            }

            // 存放最终属于该 collection 的直接第一层子分类
            List<ItemVo> rootNodes = new ArrayList<>();

            // 步骤 2：第二遍遍历，在内存中“拉线织网”组装树
            for (SolrDocument doc : flatResults) {
                String currentId = getObjectString(doc.get("id"));
                ItemVo currentVo = allVoMap.get(currentId);

                // 合并提取当前节点关联的所有父级级标识 (兼容 memberOf 和 parentRefs)
                Set<String> fatherUris = new HashSet<>();
                fatherUris.addAll(extractMultiValues(doc.get("memberOf")));
                fatherUris.addAll(extractMultiValues(doc.get("parentRefs")));

                boolean isFirstLevel = false;

                // 遍历所有的父引脚
                for (String fatherUri : fatherUris) {
                    // 判定 A：如果当前节点的父列表中包含传入的核心 collectionUri，说明它是【第一层子分类】
                    if (collectionUri.equals(fatherUri)) {
                        isFirstLevel = true;
                    }

                    // 判定 B：去 Map 里看它的爸爸在不在结果集里（适用于孙分类找子分类）
                    ItemVo parentVo = allVoMap.get(fatherUri);
                    if (parentVo != null) {
                        if (!parentVo.getChildren().contains(currentVo)) {
                            parentVo.getChildren().add(currentVo);
                        }
                    }
                }

                // 如果是直接子分类，塞入最终返回的 root 列表中
                if (isFirstLevel && !rootNodes.contains(currentVo)) {
                    rootNodes.add(currentVo);
                }
            }

            // 步骤 3：计算 childCount 数量
            allVoMap.values().forEach(vo -> {
                if (vo.getChildren() != null) {
                    vo.setChildCount((long) vo.getChildren().size());
                }
            });

            System.out.println("====== [Concepts] 最终组装出的第一层数量: " + rootNodes.size() + " ======");
            return rootNodes;

        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 辅助工具：安全提取多值或单值字段
     */
    private List<String> extractMultiValues(Object obj) {
        List<String> values = new ArrayList<>();
        if (obj instanceof Collection<?>) {
            ((Collection<?>) obj).forEach(o -> {
                if (o != null) values.add(getObjectString(o));
            });
        } else if (obj != null) {
            values.add(getObjectString(obj));
        }
        return values;
    }


//    private List<ItemVo> getConceptsByCollection(String collection) {
//        // 1. 先去 Solr 里查一下这个 codeListId 本身是个什么 doctype
//        String queryStr = "memberOf:\"" + collection + "\"";
//
//        // 2. 智能化自动变阵（对前端完全隐蔽）
//        SolrQuery query = new SolrQuery(queryStr);
//        query.setRows(1000);
//        query.setFields("id", "localName", "zh_label");
//
//
//        try {
//            QueryResponse queryResponse = solrClient.query("concepts", query);
//
//
//            Map<String, Long> count = this.count(queryResponse.getBeans(ConceptType.class).stream().map(ConceptType::getUri).collect(Collectors.toSet()));
//
//
//            return queryResponse.getBeans(ConceptType.class).stream()
//                    .map(m ->
//                            ItemVo.builder()
//                                    .name(m.getLocalName())
//                                    .label(m.getLabel().get("zh_label"))
//                                    .childCount(count.getOrDefault(m.getUri(), 0L))
//                                    .build())
//                    .toList();
//        } catch (SolrServerException | IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private boolean isNullString(String str) {
        return str == null || str.isEmpty() || str.equals("null") || str.equals("undefined");
    }

    private Map<String, Long> count(Set<String> ids) {


// 2. 【核心优化】不再拼 OR，直接用逗号把所有 URI 连成一根干净的字符串
        String idsCommaStr = String.join(",", ids);
        JsonQueryRequest request = new JsonQueryRequest()
                .setQuery(
                        "{!terms f=memberOf}" + idsCommaStr +
                                " OR {!terms f=broader}" + idsCommaStr
                )
                .setLimit(0)
                .withFacet(
                        "memberOf_count",
                        new TermsFacetMap("memberOf")
                                .setLimit(ids.size())
                )
                .withFacet(
                        "broader_count",
                        new TermsFacetMap("broader")
                                .setLimit(ids.size())
                );
        try {
            Map<String,Long> count=new HashMap<>();
            NestableJsonFacet nestableJsonFacet = request.process(solrClient, "concepts").getJsonFacetingResponse();
            nestableJsonFacet.getBucketBasedFacetNames().forEach(facetName -> {
                nestableJsonFacet.getBucketBasedFacets(facetName).getBuckets().forEach(bucket -> {
                    count.merge(bucket.getVal().toString(),bucket.getCount(),Long::sum);
                });
            });
            return count;
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
