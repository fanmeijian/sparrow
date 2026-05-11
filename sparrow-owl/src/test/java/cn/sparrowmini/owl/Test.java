package cn.sparrowmini.owl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {
    @org.junit.jupiter.api.Test
    public void test4() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OwlParserService owlParserService = OwlParserService
                .builder()
                .ns(ns)
                .ontologyPath(owlPath)
                .build();
        System.out.println(mapper.writeValueAsString(owlParserService.getChildClasses("ProductStandard")));
    }

    @org.junit.jupiter.api.Test
    public void test3() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OwlParserService owlParserService = OwlParserService
                .builder()
                .ns(ns)
                .ontologyPath(owlPath)
                .build();
        System.out.println(mapper.writeValueAsString(owlParserService.getRootClasses()));
    }

}
