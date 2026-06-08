package cn.sparrowmini.owl.solr.service;

import cn.sparrowmini.common.dto.ItemVo;
import cn.sparrowmini.common.dto.PropertyVo;
import cn.sparrowmini.common.dto.RestrictionDto;
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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocument;
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
    public List<ItemVo> getChildrenByClassId(String parentId) {
        String parentUri = getUri(parentId);
        String query = "*:*";
        if (parentId == null) {
            query = "doctype:class AND -parents:[* TO *]";
        } else {
            query = "doctype:class AND parents:\"" + parentUri + "\"";
        }
        Set<String> fields = Set.of("localName", "zh_label");
        SolrQuery queryChild = new SolrQuery(query);
        queryChild.setFields(fields.toArray(new String[0]));
        queryChild.setRows(1000);
        try {
            QueryResponse response = solrClient.query("class", queryChild);
            return response.getResults().stream().map(doc ->
                    ItemVo.builder()
                            .name(getObjectString(doc.get("localName")))
                            .label(getObjectString(doc.get("zh_label")))
                            .build()
            ).toList();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getObjectString(Object object) {
        return object == null ? "" : object.toString();
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
        // 1. 先去 Solr 里查一下这个 codeListId 本身是个什么 doctype
        String queryStr = "memberOf:\"" + collection + "\"";

        // 2. 智能化自动变阵（对前端完全隐蔽）
        SolrQuery query = new SolrQuery(queryStr);
        query.setRows(1000);
        query.setFields("id", "localName", "zh_label");


        try {
            QueryResponse queryResponse = solrClient.query("concepts", query);


            Map<String, Long> count = this.count();


            return queryResponse.getBeans(ConceptType.class).stream()
                    .map(m ->
                            ItemVo.builder()
                                    .name(m.getLocalName())
                                    .label(m.getLabel().get("zh_label"))
                                    .childCount(count.getOrDefault(m.getUri(), 0L))
                                    .build())
                    .toList();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isNullString(String str) {
        return str == null || str.isEmpty() || str.equals("null") || str.equals("undefined");
    }

    private Map<String, Long> count() {

// 1. 假设这是你第一步查出来的子节点 ID 集合
        Set<String> ids = Set.of(
                "http://www.cn-plc.com/ontology/cms#SgccCollection",
                "http://www.cn-plc.com/ontology/cms#CsgCollection",
                "http://www.cn-plc.com/ontology/cms#PS_040"
        );

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
