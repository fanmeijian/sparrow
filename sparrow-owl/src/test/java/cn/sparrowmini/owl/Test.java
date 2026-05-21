package cn.sparrowmini.owl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

    @org.junit.jupiter.api.Test
    public void test8() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OwlParserService owlParserService = OwlParserService
                .builder()
                .ns(ns)
                .ontologyPath(owlPath)
                .build();
        System.out.println(mapper.writeValueAsString(owlParserService.getAllClass()));

    }

    @org.junit.jupiter.api.Test
    public void test7() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OwlParserService owlParserService = OwlParserService
                .builder()
                .ns(ns)
                .ontologyPath(owlPath)
                .build();
        System.out.println(mapper.writeValueAsString(owlParserService.getAllProperties()));

    }

    @org.junit.jupiter.api.Test
    public void test6() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OwlParserService owlParserService = OwlParserService
                .builder()
                .ns(ns)
                .ontologyPath(owlPath)
                .build();
        System.out.println(mapper.writeValueAsString(owlParserService.getClassTree("Menu")));

    }

    @org.junit.jupiter.api.Test
    public void test5() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OwlParserService owlParserService = OwlParserService
                .builder()
                .ns(ns)
                .ontologyPath(owlPath)
                .build();
//        System.out.println(mapper.writeValueAsString(owlParserService.getChildClasses("M_001")));
//        System.out.println(mapper.writeValueAsString(owlParserService.getAllChildren("M_001")));
        System.out.println(mapper.writeValueAsString(owlParserService.getOwlClass("M_003_001")));

    }

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
        System.out.println(mapper.writeValueAsString(owlParserService.getChildClasses("ProductArticle")));
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
