package cn.sparrowmini.owl.jpa;

import cn.sparrowmini.owl.jpa.service.OwlParserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.*;
import org.apache.jena.rdf.model.Resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {

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

//    @org.junit.jupiter.api.Test
//    public void testAll() {
//        ObjectMapper mapper = new ObjectMapper();
//        OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_RDFS_INF);
//        String owlPath = "/cms-ontology.owl";
//        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
//        try (InputStream in = Test.class.getResourceAsStream(owlPath)) {
//            model.read(in, ns, "RDF/XML");
//
//            model.classes().filter(c -> c.isHierarchyRoot()).forEach(c -> {
//                System.out.println("root:" + c.getLocalName());
//                OwlClassV2Tree owlClassV2Tree = new OwlClassV2Tree();
//                buildOwlClassV2Tree(owlClassV2Tree, c);
////                Map<String, Object> result = getPropertiesByClass(model, c.getLocalName());
//
//                try {
//                    System.out.println(mapper.writeValueAsString(owlClassV2Tree));
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @org.junit.jupiter.api.Test
    public void get111() {
        ObjectMapper mapper = new ObjectMapper();
        OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_RDFS_INF);
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        try (InputStream in = Test.class.getResourceAsStream(owlPath)) {
            model.read(in, ns, "RDF/XML");

            model.classes().filter(c -> c.isHierarchyRoot()).forEach(c -> {
                System.out.println("root:" + c.getLocalName());
                Map<String, Object> result = getPropertiesByClass(model, c.getLocalName());

                try {
                    System.out.println(mapper.writeValueAsString(result));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });

//            Map<String, Object > result= getPropertiesByClass(model,"ProductStandard");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @org.junit.jupiter.api.Test
    public void getClassByName() {
        OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_RDFS_INF);
        String owlPath = "/cms-ontology.owl";
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        try (InputStream in = Test.class.getResourceAsStream(owlPath)) {
            model.read(in, ns, "RDF/XML");

            model.classes().forEach(ontClass -> {
                System.out.println("表单名称 (Class): " + ontClass.getLabel(null));

                // 获取该类关联的所有属性（包括继承自父类的）
                ontClass.properties().forEach(prop -> {
                    System.out.println("  --- 字段属性 ---");
                    System.out.println("  ID: " + prop.getLocalName());
                    System.out.println("  显示名称: " + prop.getLabel("zh")); // 获取中文标签

                    // 1. 判断数据类型 (DatatypeProperty)
                    if (prop.canAs(OntObjectProperty.class)) {
                        prop.ranges().forEach(range -> {
                            System.out.println("  输入类型: 文本/数字 (Range: " + (range != null ? range.getLocalName() : "String") + ")");

                        });
                    }

                    // 2. 判断对象引用 (ObjectProperty) -> 用于下拉框
                    if (prop.canAs(OntDataProperty.class)) {
                        OntDataProperty dataProp = prop.as(OntDataProperty.class);
                        System.out.println("  [数据属性] " + dataProp.getLocalName() + " -> 数据类型: " + dataProp.ranges().findFirst().orElse(null));
//                        System.out.println("  输入类型: 下拉列表 (关联类: " + prop.ranges().as(OntClass.class).getLocalName() + ")");
                    }

                    // 3. 判断是否必填 (基于 OWL Restriction)
                    // 在 Jena 5 中可以通过检测是否具有 minCardinality >= 1 的约束来实现
                });
            });

//            model.read(in, ns, "RDF/XML-ABBREV");
//            model.classes().forEach(c->{
//                System.out.println(c.getLocalName());
//                c.properties().forEach(p->{
//                    System.out.println(p.getLocalName());
//                });
//
//            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

//    private OwlClassV2Tree getPropertiesByClassV2(OntModel model, String className) {
//        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
//        org.apache.jena.ontapi.model.OntClass.Named ontClass = model.getOntClass(ns + className);
//
//        OwlClassV2Tree owlClassV2 = new OwlClassV2Tree();
//        owlClassV2.setLabel(ontClass.getLabel("zh"));
//        owlClassV2.setName(ontClass.getLocalName());
//        owlClassV2.setChildren();
//    }
//
//    private void buildOwlClassV2Tree(OwlClassV2Tree owlClassV2Tree, OntClass ontClass) {
//        List<OwlClassV2Tree> children = new ArrayList<>();
//        ontClass.subClasses().forEach(subClass -> {
//            OwlClassV2Tree child = new OwlClassV2Tree();
//            child.setLabel(subClass.getLabel("zh"));
//            child.setName(subClass.getLocalName());
//            children.add(child);
//            buildOwlClassV2Tree(child, subClass);
//        });
//        owlClassV2Tree.setChildren(children);
//        owlClassV2Tree.setProperties(getPropertiesV2(ontClass.asNamed()));
//    }


    private Map<String, Object> getPropertiesByClass(OntModel model, String className) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> subclasses = new ArrayList<>();
        List<Map<String, Object>> properties = new ArrayList<>();

        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        org.apache.jena.ontapi.model.OntClass.Named ontClass = model.getOntClass(ns + className);

        if (ontClass == null) {
            result.put("error", "Class not found: " + className);
            return result;
        }

        result.put("className", className);
        result.put("label", ontClass.getLabel(null));

        ontClass.subClasses(true).forEach(subClass -> {
            String subName = subClass.getLocalName();
//            if (Set.of("Thing", "Nothing", "Resource", "Property", "Class", "Ontology").contains(subName)) continue;
            Map<String, Object> subMap = new HashMap<>();
            subMap.put("name", subName);
            subMap.put("label", subClass.getLabel("zh"));
            subclasses.add(subMap);
//            List<Map<String, Object>> subproperties = new ArrayList<>();
//            subClass.properties().forEach(prop -> {
//                Map<String, Object> propMap = new HashMap<>();
//                propMap.put("code", prop.getLocalName());
//                propMap.put("name", prop.getLabel("zh"));
//                subproperties.add(propMap);
//
//            });
            subMap.put("properties", this.getProperties(subClass.asNamed()));
        });


//        ontClass.properties().forEach(prop -> {
//            Map<String, Object> propMap = new HashMap<>();
//            propMap.put("code", prop.getLocalName());
//            propMap.put("name", prop.getLabel("zh"));
////            propMap.put("type", prop.isObjectProperty() ? "object" : "data");
//            properties.add(propMap);
//        });


        result.put("subclasses", subclasses);
        result.put("properties", getProperties(ontClass));
        return result;
    }

    private List<Map<String, Object>> getProperties(OntClass.Named ontClass) {
        List<Map<String, Object>> result = new ArrayList<>();
        ontClass.properties().forEach(prop -> {
            Map<String, Object> propMap = new HashMap<>();
            propMap.put("code", prop.getLocalName());
            propMap.put("name", prop.getLabel("zh"));
            propMap.put("type", prop.canAs(OntObjectProperty.class) ? "object" : "data");
            propMap.put("range", prop.ranges().map(Resource::getLocalName).toList());
            result.add(propMap);
        });
        return result;
    }

//    private List<OwlPropertyV2> getPropertiesV2(OntClass.Named ontClass) {
//        return ontClass.properties().map(prop -> {
//            OwlPropertyV2 owlPropertyV2 = new OwlPropertyV2();
//            owlPropertyV2.setLabel(prop.getLabel("zh"));
//            owlPropertyV2.setName(prop.getLocalName());
//            owlPropertyV2.setType(prop.canAs(OntObjectProperty.class) ? OwlPropertyTypeEnum.OBJECT : OwlPropertyTypeEnum.DATA);
//            owlPropertyV2.setRanges(prop.ranges().map(Resource::getLocalName).toList());
//            return owlPropertyV2;
//        }).toList();
//    }
}
