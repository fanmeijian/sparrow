package cn.sparrowmini.owl.solr;

import cn.sparrowmini.owl.solr.service.OntologyIndexService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.io.IOException;
import java.util.*;

public class Test {
    List<String> solrUrl = List.of("http://localhost:8983/solr");

    SolrClient solrClient = new CloudHttp2SolrClient.Builder(solrUrl)
            .build();

    @org.junit.jupiter.api.Test
    public void testPrintClassAndProp() throws SolrServerException, IOException {

        SolrQuery query = new SolrQuery("doctype:class AND -parents:[* TO *]");
        query.setFields(new String[]{"id,localName,zh_label"});
        QueryResponse response = solrClient.query("class", query);
        response.getResults().forEach(f->{
            System.out.println("aa"+f.get("zh_label") + f.get("localName").toString());
            String parentUri= f.get("id").toString();

            try {
                getPropertiesByClass(f.get("id").toString(), List.of());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            SolrQuery queryChild = new SolrQuery("doctype:class AND parents:\"" + parentUri + "\"");
            try {
                QueryResponse  response1 = solrClient.query("class", queryChild);
                response1.getResults().forEach(f1->{
                    System.out.println("  --" + f1.get("zh_label") + f1.get("localName").toString());
                    try {
                        getPropertiesByClass(f1.get("id").toString(), List.of());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (SolrServerException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void getPropertiesByClass(String currentClassUri, Collection<String> allParentUris) throws Exception {
        StringBuilder queryBuilder = new StringBuilder();

        // 1. 匹配直接用于当前分类的属性
        queryBuilder.append("used_in:\"").append(currentClassUri).append("\"");

        // 2. 顺带匹配用于其所有祖先分类的继承属性（让当前分类自动拥有父类的属性）
//        if (allParentUris != null && !allParentUris.isEmpty()) {
//            for (String parentUri : allParentUris) {
//                queryBuilder.append(" OR used_in:\"").append(parentUri).append("\"");
//            }
//        }

        SolrQuery query = new SolrQuery(queryBuilder.toString());
        query.setRows(1000); // 属性通常不会上千，一次性全部捞回

        // 🚀 核心变化：直接去查 "props" 集合，一步到位拿到带完整元数据的 Property
        QueryResponse response = solrClient.query("props", query);
        System.out.println(currentClassUri + response.getResults().size());
        response.getResults().forEach(f->{
           System.out.println("  ==" + f.get("zh_label"));
        });
    }

    @org.junit.jupiter.api.Test
    public void testInitSolr1() throws IOException {

//        List<String> solrUrls = List.of("http://localhost:8983/solr");
//        SolrClient solrClient = new CloudHttp2SolrClient.Builder(solrUrls)
//                .build();
//        OntologyIndexService ontologyIndexService = new OntologyIndexService(solrClient);
//        OntologyIndexService.initCollections(solrClient);
//        ontologyIndexService.createIndex("/cms-ontology.owl");

        // 1. 依然填写你远程服务器暴露出来的公网 HTTP 地址
        String solrUrl = "http://159.75.17.246:8983/solr";

        // 2. 纯原生 JDK 客户端构建，零外部重量级依赖
        try (HttpJdkSolrClient client = new HttpJdkSolrClient.Builder(solrUrl)
                .build()) {
            OntologyIndexService ontologyIndexService = new OntologyIndexService(client);
            OntologyIndexService.initCollections(client);
            ontologyIndexService.createIndex("/cms-ontology.owl");

        }


    }

}
