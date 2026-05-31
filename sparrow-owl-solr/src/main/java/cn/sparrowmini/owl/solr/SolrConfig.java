package cn.sparrowmini.owl.solr;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SolrConfig {

    @Bean
    public SolrClient solrClient() {
        // 替换为你实际的 Solr 服务器基础 URL
        List<String> solrUrl = List.of("http://localhost:8983/solr");

        return new CloudHttp2SolrClient.Builder(solrUrl)
                .build();
    }
}