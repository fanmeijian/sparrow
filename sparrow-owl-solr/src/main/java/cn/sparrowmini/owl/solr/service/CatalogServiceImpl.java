package cn.sparrowmini.owl.solr.service;

import cn.sparrowmini.common.dto.ItemVo;
import cn.sparrowmini.common.dto.PropertyVo;
import cn.sparrowmini.common.service.CatalogService;
import cn.sparrowmini.common.util.JsonUtils;
import cn.sparrowmini.owl.solr.model.ConceptType;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CatalogServiceImpl implements CatalogService {
    private final SolrClient solrClient;
    private final String ns="http://www.cn-plc.com/ontology/cms#";

    @Override
    public List<ItemVo> getChildrenByClassId(String parentId) {
        String parentUri = getUri(parentId) ;
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
            return response.getResults().stream().map(doc->{
               return new ItemVo(getObjectString(doc.get("localName")), getObjectString(doc.get("zh_label")));
            }).toList();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getObjectString(Object object) {
        return object == null ? "" : object.toString();
    }

    @Override
    public List<PropertyVo> getPropertiesByClassId(String catalogId) {
        String currentClassUri = getUri(catalogId) ;
        // 1. 匹配直接用于当前分类的属性
        SolrQuery query = new SolrQuery("used_in:\"" + currentClassUri + "\"");
        query.setRows(1000); // 属性通常不会上千，一次性全部捞回
        try {
            // 🚀 核心变化：直接去查 "props" 集合，一步到位拿到带完整元数据的 Property
            QueryResponse response = solrClient.query("props", query);
            return response.getResults().stream().map(doc->{
                return new PropertyVo(
                        doc.get("localName").toString(),
                        doc.get("zh_label").toString(),
                        doc.get("propType").toString(),
                        doc.get("valueQualifier") == null? null:doc.get("valueQualifier").toString(),
                        (boolean) doc.get("isFacet"),
                        (boolean) doc.get("isRequired"),
                        (boolean) doc.get("isVisible"),
                        doc.get("codeListId")==null? null: doc.get("codeListId").toString(),
                        (List<String>) doc.get("range")
                );
            }).toList();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ItemVo> getCodeTypeByCodeListId(String codeListId) {
        String parentUri = getUri(codeListId) ;
        String query = "codedList:\"" + parentUri + "\"";
        Set<String> fields = Set.of("localName", "zh_label");
        SolrQuery queryChild = new SolrQuery(query);
        queryChild.setFields(fields.toArray(new String[0]));
        queryChild.setRows(1000);
        try {
            QueryResponse response = solrClient.query("codes", queryChild);
            return response.getResults().stream().map(doc->{
                return new ItemVo(doc.get("localName").toString(),doc.get("zh_label").toString());
            }).toList();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAllChildrenIdByParentClassId(String parentId) {
        try {
           SolrDocument solrDocument = solrClient.getById("class",getUri(parentId));
           return (List<String>) solrDocument.getFieldValue("allChildren");
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUri(String name){
        return name==null || name.contains(ns)? name: ns + name;
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
}
