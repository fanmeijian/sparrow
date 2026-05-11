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

    private OwlSchemaService() {}

    public static OwlSchemaService instance(String owlPath, String ns) {
        OwlSchemaService instance = new OwlSchemaService();
        instance.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
        instance.ns = ns;

        try (InputStream in = instance.getClass().getResourceAsStream(owlPath)) {
            instance.model.read(in, ns, "RDF/XML");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    public Map<String, Object> buildSchemaBundle(String classCode) {

        OntClass cls = model.getOntClass(classCode);
        if (cls == null) throw new RuntimeException("Class not found: " + classCode);

        Map<String, Object> result = new HashMap<>();
        result.put("jsonSchema", buildJsonSchema(cls));
        result.put("uiSchema", buildUiSchema(cls));

        return result;
    }

    private String ns(String local) {
        return this.ns + local;
    }

    // =========================================================
    // JSON SCHEMA
    // =========================================================

    private Map<String, Object> buildJsonSchema(OntClass cls) {

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("title", cls.getLocalName());

        Map<String, Object> properties = new LinkedHashMap<>();

        ExtendedIterator<OntProperty> props = model.listAllOntProperties();

        while (props.hasNext()) {
            OntProperty p = props.next();

            if (isSystemProperty(p)) continue;
            if (!isApplicableProperty(p, cls)) continue;

            properties.put(p.getLocalName(), buildField(p));
        }

        schema.put("properties", properties);
        schema.put("required", new ArrayList<>());

        return schema;
    }

    // =========================================================
    // ⭐ 核心修复：真正 OWL domain 判断
    // =========================================================

    private boolean isApplicableProperty(OntProperty p, OntClass cls) {

        ExtendedIterator<? extends OntResource> domains = p.listDomain();

        while (domains.hasNext()) {

            OntResource d = domains.next();
            if (!d.canAs(OntClass.class)) continue;

            OntClass domain = d.as(OntClass.class);

            // ⭐ inference check
            if (cls.hasSuperClass(domain)
                    || cls.hasEquivalentClass(domain)
                    || domain.hasSuperClass(cls)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInUnionOrIntersection(OntClass dc, OntClass cls) {

        // =========================
        // 1. unionOf 结构
        // =========================
        StmtIterator it = dc.listProperties(OWL.unionOf);

        while (it.hasNext()) {

            Statement stmt = it.nextStatement();
            RDFNode node = stmt.getObject();

            if (node.canAs(RDFList.class)) {
                RDFList list = node.as(RDFList.class);

                if (matchRdfList(list, cls)) {
                    return true;
                }
            }
        }

        // =========================
        // 2. intersectionOf 结构
        // =========================
        it = dc.listProperties(OWL.intersectionOf);

        while (it.hasNext()) {

            Statement stmt = it.nextStatement();
            RDFNode node = stmt.getObject();

            if (node.canAs(RDFList.class)) {
                RDFList list = node.as(RDFList.class);

                if (matchIntersectionList(list, cls)) {
                    return true;
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

            // =========================
            // 核心语义判断
            // =========================

            // ✔ 完全相等
            if (cls.equals(c)) {
                return true;
            }

            // ✔ 子类匹配（关键）
            if (cls.hasSuperClass(c)) {
                return true;
            }

            // ✔ 反向推理（避免 OWL 层级漏匹配）
            if (c.hasSuperClass(cls)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSameClass(OntClass a, OntClass b) {
        return a.getURI().equals(b.getURI());
    }

    // =========================================================
    // ⭐ OWL union / intersection support
    // =========================================================

    private boolean matchUnionOrIntersection(Resource anon, OntClass cls) {

        Statement unionStmt = anon.getProperty(OWL.unionOf);
        if (unionStmt != null) {
            RDFNode node = unionStmt.getObject();
            if (node.canAs(RDFList.class)) {
                return matchUnionList(node.as(RDFList.class), cls);
            }
        }

        Statement interStmt = anon.getProperty(OWL.intersectionOf);
        if (interStmt != null) {
            RDFNode node = interStmt.getObject();
            if (node.canAs(RDFList.class)) {
                return matchIntersectionList(node.as(RDFList.class), cls);
            }
        }

        return false;
    }

    private boolean matchUnionList(RDFList list, OntClass cls) {

        for (Iterator<RDFNode> it = list.iterator(); it.hasNext(); ) {

            RDFNode node = it.next();

            if (!node.canAs(OntClass.class)) continue;

            OntClass c = node.as(OntClass.class);

            if (isSameOrSubClass(cls, c)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchIntersectionList(RDFList list, OntClass cls) {

        // intersectionOf：必须全部满足
        for (Iterator<RDFNode> it = list.iterator(); it.hasNext(); ) {

            RDFNode node = it.next();

            if (!node.canAs(OntClass.class)) continue;

            OntClass c = node.as(OntClass.class);

            if (!isSameOrSubClass(cls, c)) {
                return false;
            }
        }

        return true;
    }

    private boolean isSameOrSubClass(OntClass target, OntClass base) {
        return target.equals(base) || target.hasSuperClass(base);
    }

    // =========================================================
    // FIELD BUILD
    // =========================================================

    private Map<String, Object> buildField(OntProperty p) {

        if (p.isDatatypeProperty()) {
            return buildDataField(p);
        }
        return buildObjectField(p);
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

        field.put("type", ranges.size() <= 1 ? "string" : "array");
        field.put("items", Map.of("type", "string"));

        field.put("x-range",
                ranges.stream().map(OntClass::getLocalName).toList()
        );

        return field;
    }

    // =========================================================
    // UI SCHEMA（不变）
    // =========================================================

    private Map<String, Object> buildUiSchema(OntClass cls) {

        Map<String, Object> ui = new LinkedHashMap<>();

        ExtendedIterator<OntProperty> props = model.listAllOntProperties();

        while (props.hasNext()) {

            OntProperty p = props.next();

            if (isSystemProperty(p)) continue;
            if (!isApplicableProperty(p, cls)) continue;

            ui.put(p.getLocalName(), buildUiField(p));
        }

        return ui;
    }

    private Map<String, Object> buildUiField(OntProperty p) {

        Map<String, Object> ui = new HashMap<>();

        if (p.isDatatypeProperty()) {

            OntResource range = p.getRange();

            ui.put("ui:widget",
                    range != null && "dateTime".equalsIgnoreCase(range.getLocalName())
                            ? "date"
                            : "text"
            );

            return ui;
        }

        ui.put("ui:widget", "treeSelect");
        ui.put("ui:options", Map.of("tree", Map.of("code", "ROOT")));

        return ui;
    }

    // =========================================================
    // UTIL
    // =========================================================

    private boolean isSystemProperty(OntProperty p) {
        String ns = p.getNameSpace();
        return ns != null && (ns.contains("rdf") || ns.contains("rdfs") || ns.contains("owl"));
    }

    private List<OntClass> getRanges(OntProperty p) {

        List<OntClass> list = new ArrayList<>();

        ExtendedIterator<? extends OntResource> it = p.listRange();

        while (it.hasNext()) {

            OntResource r = it.next();

            if (r.canAs(OntClass.class)) {
                list.add(r.as(OntClass.class));
            }
        }

        return list;
    }
}