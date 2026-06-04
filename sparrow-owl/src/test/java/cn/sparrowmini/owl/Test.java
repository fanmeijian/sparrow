package cn.sparrowmini.owl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

public class Test {

    @org.junit.jupiter.api.Test
    public void testPrintClass() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
//        String owlPath = "/FurnitureSectorTaxonomy-v2.5.1.owl";
//        String ns = "http://www.aidimme.es/FurnitureSectorOntology.owl#";
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OwlParserService owlParserService = OwlParserService
                .builder()
                .ns(ns)
                .ontologyPath(owlPath)
                .build();
//        System.out.println(mapper.writeValueAsString(owlParserService.printAllClasses()));
//        owlParserService.printAllClasses();
//        owlParserService.printAllProperties();
//        owlParserService.rest("AssociationStandard");
        ;
//        System.out.println(mapper.writeValueAsString(owlParserService.printClass("PS_006")));
        owlParserService.printRestrictions();
    }

    @org.junit.jupiter.api.Test
    public void test66() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
//        String owlPath = "/FurnitureSectorTaxonomy-v2.5.1.owl";
//        String ns = "http://www.aidimme.es/FurnitureSectorOntology.owl#";
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OwlParserService owlParserService = OwlParserService
                .builder()
                .ns(ns)
                .ontologyPath(owlPath)
                .build();
//        System.out.println(mapper.writeValueAsString(owlParserService.printAllClasses()));
//        owlParserService.printAllClasses();
//        owlParserService.printAllProperties();
//        owlParserService.rest("AssociationStandard");
        owlParserService.printPropertie("P_005");
    }

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
//    System.out.println(mapper.writeValueAsString(owlParserService.rest("")));
//        owlParserService.rest("M_001");
//        System.out.println(mapper.writeValueAsString(owlParserService.getRootClasses()));
        System.out.println(mapper.writeValueAsString(owlParserService.getOwlClass("M_001")));
//        System.out.println(mapper.writeValueAsString(owlParserService.getClassTree("M_001")));

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
        System.out.println(mapper.writeValueAsString(owlParserService.getOwlClass("M_001")));

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
