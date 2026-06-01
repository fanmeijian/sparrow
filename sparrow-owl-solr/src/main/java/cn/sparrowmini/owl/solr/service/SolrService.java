package cn.sparrowmini.owl.solr.service;

import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("solr")
public class SolrService {
    private final SolrClient solrClient;

    private String ns = "";

    @GetMapping("/classes/children")
    public List<Map<String,Object>> getSubClasses(String parentClassName,@RequestParam("fl") Set<String> fields) {
        String parentUri = ns + parentClassName;
        String query = "*:*";
        if (parentClassName == null) {
            query = "doctype:class AND -parents:[* TO *]";
        } else {
            query = "doctype:class AND parents:\"" + parentUri + "\"";
        }

        SolrQuery queryChild = new SolrQuery(query);
//        if(fields != null) {
//            queryChild.setParam("fl", String.join(",", fields));
//        }
        queryChild.setRows(1000);
        try {
            QueryResponse response = solrClient.query("class", queryChild);
            return JsonUtils.getMapper().convertValue(response.getResults(), new TypeReference<List<Map<String,Object>>>() {});
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("properties")
    public List<Map<String,Object>> getProperties(String parentClassName) {
        String currentClassUri = ns + parentClassName;
        // 1. 匹配直接用于当前分类的属性
        SolrQuery query = new SolrQuery("used_in:\"" + currentClassUri + "\"");
        query.setRows(1000); // 属性通常不会上千，一次性全部捞回
        try {
            // 🚀 核心变化：直接去查 "props" 集合，一步到位拿到带完整元数据的 Property
            QueryResponse response = solrClient.query("props", query);
            return JsonUtils.getMapper().convertValue(response.getResults(), new TypeReference<List<Map<String,Object>>>() {});

        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }


}
