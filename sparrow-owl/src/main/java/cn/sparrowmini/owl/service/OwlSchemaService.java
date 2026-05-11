package cn.sparrowmini.owl.service;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;

import java.io.InputStream;
import java.util.*;

public class OwlSchemaService {

    private OntModel model;
    private String ns;

    private OwlSchemaService() {

    }

    public static OwlSchemaService instance(String owlPath, String ns){
        OwlSchemaService  instance = new OwlSchemaService();
        instance.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
        instance.ns = ns;

        try (InputStream in = instance.getClass().getResourceAsStream(owlPath)) {
            instance.model.read(in, ns,"RDF/XML");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    public Map<String, Object> buildSchemaBundle(String classCode) {

        OntClass cls = model.getOntClass(ns(classCode));
        if (cls == null) throw new RuntimeException("Class not found: " + classCode);

        Map<String, Object> result = new HashMap<>();
        result.put("jsonSchema", buildJsonSchema(cls));
        result.put("uiSchema", buildUiSchema(cls));

        return result;
    }

    private String ns(String local) {
//        return "http://cn.liyuan.chnplc/ontology/cms#" + local;
        return this.ns + local;
    }


    private Map<String, Object> buildJsonSchema(OntClass cls) {

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("title", cls.getLocalName());

        Map<String, Object> properties = new LinkedHashMap<>();

        // 找所有 property（通过 domain）
        ExtendedIterator<OntProperty> props = model.listAllOntProperties();

        while (props.hasNext()) {
            OntProperty p = props.next();
            if (isSystemProperty(p)) continue;   // ✅ 新增
            if (!isApplicableProperty(p, cls)) continue;

            properties.put(p.getLocalName(), buildField(p));
        }

        schema.put("properties", properties);
        schema.put("required", new ArrayList<>());

        return schema;
    }
    private boolean isApplicableProperty(OntProperty p, OntClass cls) {
        ExtendedIterator<? extends OntResource> domains = p.listDomain();

        while (domains.hasNext()) {
            OntResource d = domains.next();

            // ✅ 普通 class
            if (!d.isAnon() && d.canAs(OntClass.class)) {
                OntClass dc = d.as(OntClass.class);

                // ❗只允许 当前类 或 父类（但不能跨层）
                if (cls.equals(dc)) {
                    return true;
                }

                // 👉 只允许“属性定义在父类”
                if (cls.hasSuperClass(dc) && !dc.hasSuperClass(cls)) {
                    return true;
                }
            }

            // ✅ unionOf
            if (d.isAnon()) {
                Resource anon = d.asResource();

                Statement unionStmt = anon.getProperty(OWL.unionOf);
                if (unionStmt != null) {
                    RDFNode node = unionStmt.getObject();

                    if (node.canAs(RDFList.class)) {
                        if (matchRdfList(node.as(RDFList.class), cls)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean matchRdfList(RDFList list, OntClass cls) {

        for (Iterator<RDFNode> it = list.iterator(); it.hasNext(); ) {
            RDFNode node = it.next();

            if (!node.canAs(OntClass.class)) continue;

            OntClass c = node.as(OntClass.class);

            if (c.isAnon()) continue;

            if (cls.equals(c) || cls.hasSuperClass(c)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSystemProperty(OntProperty p) {
        String ns = p.getNameSpace();

        return ns != null && (
                ns.contains("rdf") ||
                        ns.contains("rdfs") ||
                        ns.contains("owl")
        );
    }

    private Map<String, Object> buildField(OntProperty p) {

        if (p.isDatatypeProperty()) {
            return buildDataField(p);
        } else {
            return buildObjectField(p);
        }
    }


    private Map<String, Object> buildDataField(OntProperty p) {

        Map<String, Object> field = new HashMap<>();

        OntResource range = p.getRange();

        if (range == null) {
            field.put("type", "string");
            return field;
        }

        String r = range.getLocalName();

        if ("dateTime".equalsIgnoreCase(r)) {
            field.put("type", "string");
            field.put("format", "date-time");
        } else if ("int".equalsIgnoreCase(r) || "double".equalsIgnoreCase(r)) {
            field.put("type", "number");
        } else {
            field.put("type", "string");
        }

        return field;
    }

    private Map<String, Object> buildObjectField(OntProperty p) {

        Map<String, Object> field = new HashMap<>();

        List<OntClass> ranges = getRanges(p);

        if (ranges.size() <= 1) {
            field.put("type", "string");
        } else {
            field.put("type", "array");
            field.put("items", Map.of("type", "string"));
        }

        // 🔥 关键：告诉前端有哪些可选类型
        field.put("x-range", ranges.stream()
                .map(OntClass::getLocalName)
                .toList());

        return field;
    }

    private Map<String, Object> buildUiSchema(OntClass cls) {

        Map<String, Object> ui = new LinkedHashMap<>();

        ExtendedIterator<OntProperty> props = model.listAllOntProperties();

        while (props.hasNext()) {
            OntProperty p = props.next();
            if (isSystemProperty(p)) continue;   // ✅ 新增
            if (!isApplicableProperty(p, cls)) continue;

            ui.put(p.getLocalName(), buildUiField(p));
        }

        return ui;
    }

    private Map<String, Object> buildUiField(OntProperty p) {

        Map<String, Object> ui = new HashMap<>();

        if (p.isDatatypeProperty()) {

            OntResource range = p.getRange();

            if (range != null && "dateTime".equalsIgnoreCase(range.getLocalName())) {
                ui.put("ui:widget", "date");
            } else {
                ui.put("ui:widget", "text");
            }

            return ui;
        }

        // 👉 ObjectProperty → tree
        List<OntClass> ranges = getRanges(p);

        ui.put("ui:widget", "treeSelect");

        if (ranges.size() == 1) {

            ui.put("ui:options", Map.of(
                    "tree", buildTree(ranges.get(0), new HashSet<>())
            ));

        } else {

            List<Object> trees = new ArrayList<>();

            for (OntClass r : ranges) {
                Map<String, Object> tree = buildTree(r, new HashSet<>());

                if (tree != null) {   // ✅ 必须加
                    trees.add(tree);
                }
//                trees.add(buildTree(r, new HashSet<>()));
            }

            ui.put("ui:options", Map.of("trees", trees));
        }

        return ui;
    }

    private Map<String, Object> buildTree(OntClass root, Set<String> visited) {

        if (root.isAnon()) return null;

        String code = root.getLocalName();

        // 防循环
        if (!visited.add(code)) return null;

        // 过滤系统类
        if (isSystemClass(code)) return null;

        Map<String, Object> node = new HashMap<>();
        node.put("code", code);
        node.put("label", code);

        List<Object> children = new ArrayList<>();

        ExtendedIterator<OntClass> subs = root.listSubClasses(true);

        Set<String> childCodes = new HashSet<>();
        while (subs.hasNext()) {
            OntClass sub = subs.next();

            if (sub.isAnon()) continue;

            if (!childCodes.add(sub.getLocalName())) continue;  // ✅ 去重

            Map<String, Object> child = buildTree(sub, visited);
            if (child != null) {
                children.add(child);
            }
        }

        node.put("children", children);

        return node;
    }

    private boolean isSystemClass(String code) {
        return Set.of(
                "Thing", "Nothing", "Resource",
                "Property", "Class", "Ontology"
        ).contains(code);
    }

    private List<OntClass> getRanges(OntProperty p) {

        List<OntClass> list = new ArrayList<>();

        ExtendedIterator<? extends OntResource> it = p.listRange();

        while (it.hasNext()) {
            OntResource r = it.next();

            // ✅ 普通 class
            if (!r.isAnon() && r.canAs(OntClass.class)) {
                OntClass cls = r.as(OntClass.class);

                if (!isSystemClass(cls.getLocalName())) {
                    list.add(cls);
                }
            }

            // ✅ ⭐ unionOf / intersectionOf
            if (r.isAnon()) {

                Resource anon = r.asResource();

                // unionOf
                Statement unionStmt = anon.getProperty(OWL.unionOf);
                if (unionStmt != null) {
                    RDFNode node = unionStmt.getObject();

                    if (node.canAs(RDFList.class)) {
                        extractFromList(node.as(RDFList.class), list);
                    }
                }

                // intersectionOf
                Statement interStmt = anon.getProperty(OWL.intersectionOf);
                if (interStmt != null) {
                    RDFNode node = interStmt.getObject();

                    if (node.canAs(RDFList.class)) {
                        extractFromList(node.as(RDFList.class), list);
                    }
                }
            }
        }

        return list;
    }

    private void extractFromList(RDFList list, List<OntClass> result) {

        for (Iterator<RDFNode> it = list.iterator(); it.hasNext(); ) {
            RDFNode node = it.next();

            if (!node.canAs(OntClass.class)) continue;

            OntClass c = node.as(OntClass.class);

            if (c.isAnon()) continue;
            if (isSystemClass(c.getLocalName())) continue;

            result.add(c);
        }
    }
}