package cn.sparrowmini.owl.jpa.service;

import cn.sparrowmini.owl.jpa.dto.OwlClassV2;
import cn.sparrowmini.owl.jpa.dto.OwlClassV2Tree;
import cn.sparrowmini.owl.jpa.dto.OwlPropertyV2;
import cn.sparrowmini.owl.jpa.model.OwlPropertyTypeEnum;
import lombok.Builder;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.rdf.model.Resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Builder
public class OwlParserService {
    private final OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_RDFS_INF);
    private String ns;
    private String ontologyPath;

    private OwlParserService() {
    }

    private OwlParserService(String ns, String ontologyPath) {
        this.ns = ns;
        this.ontologyPath = ontologyPath;

        try (InputStream in = getClass().getResourceAsStream(ontologyPath)) {
            model.read(in, ns, "RDF/XML");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<OwlClassV2Tree> getRootClasses() {
        List<OwlClassV2Tree> owlClassV2Trees = new ArrayList<>();
        model.classes().filter(OntClass::isHierarchyRoot).forEach(c -> {
            OwlClassV2Tree owlClassV2Tree = OwlClassV2Tree.builder()
                    .name(c.getLocalName())
                    .label(c.getLabel())
                    .build();
            buildOwlClassV2Tree(owlClassV2Tree, c);
            owlClassV2Trees.add(owlClassV2Tree);
        });
        return owlClassV2Trees;
    }

    public List<OwlClassV2> getChildClasses(String className) {
        OntClass.Named cls = model.getOntClass(this.ns + className);
        if (cls == null) throw new RuntimeException("Class not found: " + this.ns + className);
        return this.getChildClasses(cls);

    }

    private List<OwlClassV2> getChildClasses(OntClass ontClass) {
        return ontClass.subClasses()
                .map(subClass -> OwlClassV2.builder()
                        .name(subClass.getLocalName())
                        .label(subClass.getLabel())
                        .properties(
                                ontClass.properties().map(prop -> OwlPropertyV2.builder()
                                                .name(prop.getLocalName())
                                                .label(prop.getLabel())
                                                .type(prop.canAs(OntObjectProperty.class) ? OwlPropertyTypeEnum.OBJECT : OwlPropertyTypeEnum.DATA)
                                                .ranges(prop.ranges().map(Resource::getLocalName).toList())
                                                .build()
                                        )
                                        .collect(Collectors.toList()) // 使用 collect 显式归集
                        )
                        .build())
                .collect(Collectors.toList()); // 使用 collect 显式归集

    }

    private void buildOwlClassV2Tree(OwlClassV2Tree owlClassV2Tree, OntClass ontClass) {
        List<OwlClassV2Tree> children = new ArrayList<>();
        ontClass.subClasses().forEach(subClass -> {
            OwlClassV2Tree child = OwlClassV2Tree.builder()
                    .name(subClass.getLocalName())
                    .label(subClass.getLabel())
                    .build();
            children.add(child);
            buildOwlClassV2Tree(child, subClass);
        });
        owlClassV2Tree.setChildren(children);
        owlClassV2Tree.setProperties(this.getProperties(ontClass.asNamed()));
    }

    private List<OwlPropertyV2> getProperties(OntClass.Named ontClass) {
        return ontClass.properties().map(prop -> OwlPropertyV2.builder()
                        .name(prop.getLocalName())
                        .label(prop.getLabel())
                        .type(prop.canAs(OntObjectProperty.class) ? OwlPropertyTypeEnum.OBJECT : OwlPropertyTypeEnum.DATA)
                        .ranges(prop.ranges().map(Resource::getLocalName).toList())
                        .build()
                )
                .collect(Collectors.toList()); // 使用 collect 显式归集

    }

}
