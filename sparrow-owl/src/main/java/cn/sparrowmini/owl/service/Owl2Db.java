package cn.sparrowmini.owl.service;


import cn.sparrowmini.owl.model.*;
import cn.sparrowmini.owl.repository.OwlClassRepository;
import cn.sparrowmini.owl.repository.OwlPropertyRepository;
import cn.sparrowmini.owl.repository.OwlRelationRepository;
import jakarta.annotation.Resource;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.sparrowmini.owl.model.OwlRelationOriginType.EXPLICIT;
import static cn.sparrowmini.owl.model.OwlRelationOriginType.INFERRED;
import static cn.sparrowmini.owl.model.OwlRelationType.*;


@Service
public class Owl2Db {
    private final static OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
    private String ns;

    @Resource
    private OwlClassRepository owlClassRepository;

    @Resource
    private OwlPropertyRepository owlPropertyRepository;

    @Resource
    private OwlRelationRepository owlRelationRepository;



    public void init(String owlFilePath, String ns) throws Exception {
//        String ns = "http://cn.liyuan.chnplc/ontology/cms#";
        this.ns = ns;
        try (InputStream in = getClass().getResourceAsStream(owlFilePath)) {
            model.read(in, ns, "RDF/XML");

            parseClass();
            parseProperty();
            parseClassRelation();
            parseUnionOf();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void parseClass() {
        ExtendedIterator<OntClass> it = model.listNamedClasses();

        while (it.hasNext()) {
            OntClass cls = it.next();

            if (cls.isAnon()) continue;

            OwlClass entity = new OwlClass();
            entity.setCode(localName(cls));
            entity.setName(localName(cls));
//            entity.setLabels(getLabels(cls));

            // ❗ 注意：这里只存“直接父类”
            entity.setParentId(getFirstParent(cls));

            owlClassRepository.save(entity);
        }
    }

    public void parseProperty() {

        ExtendedIterator<OntProperty> it = model.listOntProperties();

        while (it.hasNext()) {
            OntProperty p = it.next();

            OwlProperty op = new OwlProperty();
            op.setCode(p.getLocalName());
            op.setName(p.getLocalName());

            op.setType(p.isObjectProperty()
                    ? OwlPropertyTypeEnum.OBJECT
                    : OwlPropertyTypeEnum.DATA);

            // ❗ domain（多值）
            for (OntResource d : getDomains(p)) {
                if (!d.isAnon()) {
                    saveRelation(
                            DOMAIN_OF,
                            p.getLocalName(),
                            d.getLocalName(),
                            EXPLICIT
                    );
                }
            }

            // ❗ range（多值）
            for (OntResource r : getRanges(p)) {
                if (!r.isAnon()) {
                    saveRelation(
                            RANGE_OF,
                            p.getLocalName(),
                            r.getLocalName(),
                            EXPLICIT
                    );
                }
            }

            op.setRange(resolveRange(p));
            owlPropertyRepository.save(op);
        }
    }

    public void parseClassRelation() {

        ExtendedIterator<OntClass> it = model.listNamedClasses();

        while (it.hasNext()) {
            OntClass cls = it.next();

            ExtendedIterator<OntClass> supIt = cls.listSuperClasses(true);

            while (supIt.hasNext()) {
                OntClass sup = supIt.next();

                if (sup.isAnon()) continue;

                saveRelation(
                        SUBCLASS_OF,
                        cls.getLocalName(),
                        sup.getLocalName(),
                        EXPLICIT
                );
            }
        }
    }

    public void parsePropertyRelation() {

        ExtendedIterator<OntProperty> it = model.listOntProperties();

        while (it.hasNext()) {
            OntProperty p = it.next();

            // domain
            OntResource domain = p.getDomain();
            if (domain != null && !domain.isAnon()) {
                saveRelation(
                        DOMAIN_OF,
                        p.getLocalName(),
                        domain.getLocalName(),
                        EXPLICIT
                );
            }

            // range
            OntResource range = p.getRange();
            if (range != null && !range.isAnon()) {
                saveRelation(
                        RANGE_OF,
                        p.getLocalName(),
                        range.getLocalName(),
                        EXPLICIT
                );
            }
        }
    }

    public void parseUnionOf() {

        ExtendedIterator<OntClass> it = model.listClasses();

        while (it.hasNext()) {
            OntClass cls = it.next();

            // check anonymous expressions
            if (!cls.isAnon()) continue;

            if (cls.canAs(UnionClass.class)) {

                UnionClass uc = cls.asUnionClass();

                uc.listOperands().forEachRemaining(op -> {

                    if (!op.isAnon()) {
                        saveRelation(
                                DOMAIN_OF,
                                "UNION",
                                op.getLocalName(),
                                INFERRED
                        );
                    }
                });
            }
        }
    }

    private String getFirstParent(OntClass cls) {
        ExtendedIterator<OntClass> superIt = cls.listSuperClasses(true);

        while (superIt.hasNext()) {
            OntClass sup = superIt.next();
            if (!sup.isAnon()) {
                return sup.getLocalName();
            }
        }
        return null;
    }

    private String resolveRange(OntProperty p) {
        StmtIterator it = p.listProperties(RDFS.range);

        List<String> ranges = new ArrayList<>();

        while (it.hasNext()) {
            RDFNode node = it.next().getObject();

            if (node.isResource()) {
                String name = node.asResource().getLocalName();

                // filter OWL Thing
                if (!"Thing".equals(name)) {
                    ranges.add(name);
                }
            }
        }

        // 👉 你现在策略：flatten → 取第一个或拼接
        return String.join(",", ranges);
    }

    private void saveRelation(
            OwlRelationType type,
            String source,
            String target,
            OwlRelationOriginType origin
    ) {
        if (source == null || target == null) return;

        OwlRelation r = new OwlRelation();
        r.setId(source + "_" + type + "_" + target);
        r.setSourceCode(source);
        r.setTargetCode(target);
        r.setSourceType(resolveType(source));
        r.setTargetType(resolveType(target));
        r.setRelationType(type);
        r.setOrigin(origin);

        owlRelationRepository.save(r);
    }

    private String localName(OntClass cls) {
        if (cls == null) return null;
        return cls.isAnon() ? null : cls.getLocalName();
    }

    private String localName(OntProperty p) {
        if (p == null) return null;
        return p.getLocalName();
    }

    private Map<String, Object> getLabels(OntClass cls) {
        Map<String, Object> labels = new HashMap<>();

        cls.listLabels(null).forEachRemaining(stmt -> {
            String lang = stmt.asLiteral().getLanguage();
            String value = stmt.asLiteral().getString();
            labels.put(lang.isEmpty() ? "default" : lang, value);
        });

        return labels;
    }

    private List<OntResource> getDomains(OntProperty p) {
        List<OntResource> list = new ArrayList<>();

        p.listProperties(RDFS.domain).forEachRemaining(stmt -> {
            RDFNode node = stmt.getObject();
            if (node.isResource()) {
                list.add(node.asResource().as(OntResource.class));
            }
        });

        return list;
    }

    private List<OntResource> getRanges(OntProperty p) {
        List<OntResource> list = new ArrayList<>();

        p.listProperties(RDFS.range).forEachRemaining(stmt -> {
            RDFNode node = stmt.getObject();
            if (node.isResource()) {
                list.add(node.asResource().as(OntResource.class));
            }
        });

        return list;
    }

    private OwlRelationObjectType resolveType(String code) {
        if (model.getOntClass(ns + code) != null) {
            return OwlRelationObjectType.CLASS;
        }
        return OwlRelationObjectType.PROPERTY;
    }



}
