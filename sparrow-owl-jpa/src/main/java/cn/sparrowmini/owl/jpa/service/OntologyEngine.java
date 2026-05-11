package cn.sparrowmini.owl.jpa.service;

import cn.sparrowmini.owl.jpa.dto.OwlPropertyDto;
import cn.sparrowmini.owl.jpa.model.OwlPropertyTypeEnum;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class OntologyEngine {

    private final static OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
    private String ns;

    private OntologyEngine(){

    }

    public static OntologyEngine instance(String owlFilePath, String ns){
        try {
            return new OntologyEngine().init(owlFilePath, ns);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @PostConstruct
    private OntologyEngine init(String owlFilePath, String ns) throws Exception {
//        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        this.ns = ns;
        try (InputStream in = getClass().getResourceAsStream(owlFilePath)) {
            model.read(in, ns,"RDF/XML");
        }

        // 调试代码：强制打印模型中所有的类
        StmtIterator stmts = model.listStatements(null, RDFS.range, (RDFNode) null);

        System.out.println("---- 所有 rdfs:range ----");
        while (stmts.hasNext()) {
            Statement s = stmts.next();
            System.out.println(s.getSubject() + " -> " + s.getObject());
        }
        return this;
    }

    public List<String> getOwlClasses(String className) {
//        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        // 尝试直接通过 URI 获取
        OntClass articleClass = model.getOntClass(ns + className);

        // 如果还是 null，尝试遍历寻找（容错处理）
        if (articleClass == null) {
            articleClass = model.createClass(ns + className);
        }

        List<String> categories = new ArrayList<>();
        // 尝试将 direct 设置为 true
        ExtendedIterator<OntClass> it = articleClass.listSubClasses(true);

        while (it.hasNext()) {
            OntClass subClass = it.next();
            if (!subClass.isAnon() && !subClass.getURI().endsWith("Nothing")) {
                categories.add(subClass.getLocalName());
            }
        }
        return categories;
    }

    public Map<String, Object> getClassMetadata(String className) {
        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        OntClass ontClass = model.getOntClass(ns + className);
        Map<String, Object> metadata = new HashMap<>();

        List<Map<String, String>> propertyList = new ArrayList<>();

        // listDeclaredProperties(false) 会根据推理结果返回该类及其父类的所有相关属性
        ExtendedIterator<OntProperty> it = ontClass.listDeclaredProperties(false);

        while (it.hasNext()) {
            OntProperty prop = it.next();
            Map<String, String> pMap = new HashMap<>();
            pMap.put("name", prop.getLocalName());
            pMap.put("label", prop.getLabel("zh")); // 获取中文标签

            if (prop.isDatatypeProperty()) {
                pMap.put("type", "DATA");
                pMap.put("range", mapRangeType(prop.getRange()));
            } else if (prop.isObjectProperty()) {
                pMap.put("type", "OBJECT");
                // 获取关联的类，例如 hasStandard 关联到 ProductStandard
                pMap.put("range", prop.getRange().getLocalName());
            }

            propertyList.add(pMap);
        }

        metadata.put("className", className);
        metadata.put("properties", propertyList);
        return metadata;
    }
    public void listAllObjectProperties() {
        // 获取所有属性（包括数据属性和对象属性）
        ExtendedIterator<OntProperty> it = model.listOntProperties();

        while (it.hasNext()) {
            OntProperty prop = it.next();

            if (prop.isObjectProperty()) {
                System.out.println("对象属性名: " + prop.getLocalName());
                System.out.println("中文标签: " + prop.getLabel("zh"));

                // 获取关联的目标类 (Range)
                OntResource range = prop.getRange();
                if (range != null && range.isClass()) {
                    System.out.println("关联的目标类: " + range.getLocalName());
                }
            }
        }
    }

    public List<OwlPropertyDto> getPropertiesByClass(String classLocalName) {
//        String namespace = "http://cn.liyuan.chnplc/ontology/cms#";
        OntClass ontClass = model.getOntClass(this.ns + classLocalName);

        if (ontClass == null) return Collections.emptyList();

        List<OwlPropertyDto> properties = new ArrayList<>();

        // 关键：不要只查“声明”在该类上的属性，要查本体中所有的属性
        // 推理机会根据 Domain 定义（包括 UnionOf）自动判断适用性
        ExtendedIterator<OntProperty> it = model.listOntProperties();

        try {
            while (it.hasNext()) {
                OntProperty prop = it.next();
                // 核心判定：该属性是否适用于当前的 ontClass
                // 对于 DatatypeProperty，Jena 的推理机会处理继承和联合定义域
                if (prop.hasDomain(ontClass)) {
                    OwlPropertyDto owlPropertyDto = new OwlPropertyDto();
                    owlPropertyDto.setCode(prop.getLocalName());
                    owlPropertyDto.setName(prop.getLabel("zh"));
//                    OntResource range = prop.getRange();
//                    if (range != null) {
//                        // 1. 尝试直接获取 LocalName
//                        String name = range.getLocalName();
//
//                        // 2. 如果拿到了 Thing 或者为 null，尝试直接从 RDF 节点拿
//                        if (name == null || name.equalsIgnoreCase("Thing")) {
//                            // 直接从底层 RDF 模型中取 rdfs:range 属性的值，跳过推理计算
//                            Statement stmt = prop.getProperty(RDFS.range);
//                            if (stmt != null && stmt.getObject().isResource()) {
//                                name = stmt.getObject().asResource().getLocalName();
//                            }
//                        }
//                        owlPropertyDto.setRange(name);
//                    }

                    owlPropertyDto.setRange(this.getRealRanges(prop).stream().findFirst().orElse(null));
                    if(prop.isDatatypeProperty()) {
                        owlPropertyDto.setType( OwlPropertyTypeEnum.DATA);
                    }

                    if(prop.isObjectProperty()) {
                        owlPropertyDto.setType(OwlPropertyTypeEnum.OBJECT);
                    }
                    properties.add(owlPropertyDto);
                }
            }
        } finally {
            it.close();
        }
        return properties;
    }
    private List<String> getRealRanges(OntProperty prop) {
        StmtIterator it = prop.listProperties(RDFS.range);

        List<String> result = new ArrayList<>();

        while (it.hasNext()) {
            Statement stmt = it.next();
            RDFNode obj = stmt.getObject();

            if (!obj.isResource()) continue;

            Resource resource = obj.asResource();

            // ✅ 普通 class
            if (!resource.isAnon()) {
                String name = resource.getLocalName();

                if (name != null &&
                        !name.equals("Thing") &&
                        !name.equals("Resource")) {

                    result.add(name);
                }
            }

            // ✅ unionOf
            else {
                if (resource.canAs(OntClass.class)) {
                    OntClass cls = resource.as(OntClass.class);

                    if (cls.isUnionClass()) {
                        UnionClass uc = cls.asUnionClass();
                        ExtendedIterator<? extends OntClass> ops = uc.listOperands();

                        while (ops.hasNext()) {
                            OntClass op = ops.next();
                            if (!op.isAnon()) {
                                result.add(op.getLocalName());
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private String mapRangeType(Resource range) {
        if (range == null) return "text";
        String uri = range.getURI();
        if (uri.endsWith("string")) return "text";
        if (uri.endsWith("dateTime")) return "date";
        if (uri.endsWith("int") || uri.endsWith("integer")) return "number";
        return "text";
    }
}