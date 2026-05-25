package cn.sparrowmini.owl;

import lombok.Builder;
import org.apache.jena.base.Sys;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Builder
public class OwlParserService {
    private final OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF);
    //    private final OntModel rawModel = OntModelFactory.createModel();
    private String ns;
    private String ontologyPath;

    private final List<OwlPropertyV2> allProperties = new ArrayList<>();
    private final List<OwlClassV2> allClasses = new ArrayList<>();
    private final static Map<String, List<OwlClassV2Tree>> classCache = new ConcurrentHashMap<>();
    private final static Map<String, List<String>> allChildrenIds = new ConcurrentHashMap<>();
    private final static List<OwlClassV2Tree> rootClassTree = new ArrayList<>();

    private OwlParserService() {
    }

    private OwlParserService(String ns, String ontologyPath) {
        this.ns = ns;
        this.ontologyPath = ontologyPath;

        try (InputStream in = getClass().getResourceAsStream(ontologyPath)) {
            model.read(in, ns, "RDF/XML");
//            rawModel.read(in, ns, "RDF/XML");
            System.out.printf("....init all classes for %s...%n",ns);
            this.allClasses.addAll(getAllClass());
            System.out.printf("....init class tree for %s...%n",ns);
            this.getRootClasses();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<OwlClassV2> getAllClass() {
        return allClasses.isEmpty()? model.classes()
                .filter(f -> f.getLabel() != null)
                .map(prop ->
                        OwlClassV2.builder()
                                .name(prop.getLocalName())
                                .label(prop.getLabel())
                                .properties(getProperties(prop))
                                .rangeProperties(getAllRangeProperties(prop).stream().map(Resource::getLocalName).collect(Collectors.toList()))
                                .build()
                ).collect(Collectors.toList())
                : allClasses;
    }

    public List<OwlPropertyV2> getAllProperties() {
        if (allProperties.isEmpty()) {
            List<OwlPropertyV2> allProperties_ = model.properties()
                    .filter(f -> f.getLabel() != null)
                    .map(prop ->
                            OwlPropertyV2.builder()
                                    .name(prop.getLocalName())
                                    .label(prop.getLabel())
                                    .ranges(prop.ranges().map(Resource::getLocalName).collect(Collectors.toList()))
                                    .domains(prop.domains().map(Resource::getLocalName).collect(Collectors.toList()))
                                    .type(prop.canAs(OntObjectProperty.class) ? OwlPropertyTypeEnum.OBJECT : OwlPropertyTypeEnum.DATA)
                                    .build()
                    ).collect(Collectors.toList());
            allProperties.addAll(allProperties_);
        }

        return allProperties;
    }

    public List<OwlClassV2Tree> getRootClasses() {
        List<OwlClassV2Tree> owlClassV2Trees = rootClassTree;
        if(owlClassV2Trees.isEmpty()){
            model.classes().filter(
                    cls->cls.isHierarchyRoot()&& cls.getNameSpace().startsWith(this.ns)
            ).forEach(rootClass -> {
                OwlClassV2Tree owlClassV2Tree = OwlClassV2Tree.builder()
                        .name(rootClass.getLocalName())
                        .label(rootClass.getLabel())
                        .equivalentClasses(rootClass.equivalentClasses().map(OntClass::getLocalName).toList())
                        .isEquivalentClassesDirect(true)
                        .build();
                buildOwlClassV2Tree(owlClassV2Tree, rootClass);
//                owlClassV2Tree.setChildren(new ArrayList<>());
//                owlClassV2Tree.getChildren().addAll(this.getClassTree(c));
                owlClassV2Trees.add(owlClassV2Tree);
            });
        }

        return owlClassV2Trees;
    }

    public List<OwlClassV2Tree> getClassTree(String className) {
        OntClass.Named cls = model.getOntClass(this.ns + className);
        if (cls == null) throw new RuntimeException("Class not found: " + this.ns + className);
        List<OwlClassV2Tree> owlClassV2Trees = classCache.get(className);
        if (owlClassV2Trees == null) {
            owlClassV2Trees = this.getClassTree(cls);
            classCache.put(className, owlClassV2Trees);

        }
        return owlClassV2Trees;
    }

    private List<OwlClassV2Tree> getClassTree(OntClass.Named cls) {
//        OwlClassV2Tree owlClassV2Tree = OwlClassV2Tree.builder()
//                .name(cls.getLocalName())
//                .label(cls.getLabel())
//                .build();
//        buildOwlClassV2Tree(owlClassV2Tree, cls);
//        return owlClassV2Tree.getChildren();
        return buildOwlClassV2Tree(cls);
    }

    public OwlClassV2 getOwlClass(String className) {
        OntClass.Named cls = model.getOntClass(this.ns + className);
        if (cls == null) throw new RuntimeException("Class not found: " + this.ns + className);

        return getOwlClass(cls);
    }

    private OwlClassV2 getOwlClass(OntClass ontClass) {
        return OwlClassV2.builder()
                .name(ontClass.getLocalName())
                .label(ontClass.getLabel())
                .properties(getProperties(ontClass.asNamed()))
                .rangeProperties(getAllRangeProperties(ontClass).stream().map(Resource::getLocalName).collect(Collectors.toList()))
                .build();
    }


    public List<OwlClassV2> getChildClasses(String className) {
        OntClass.Named cls = model.getOntClass(this.ns + className);

//        System.out.println("className" + cls.subClasses().count() + cls.equivalentClasses().map(m -> m.getLabel()).toList());
//        if (cls == null) throw new RuntimeException("Class not found: " + this.ns + className);
        if(cls!=null){
            return this.getChildClasses(cls);
        }else{
            return Collections.emptyList();
        }

    }

    public List<OwlClassV2> getAllChildren(String className) {
        OntClass.Named cls = model.getOntClass(this.ns + className);
        if (cls == null) throw new RuntimeException("Class not found: " + this.ns + className);
        return this.getAllChildren(cls);
    }

    public List<String> getAllChildrenIds(String className){
        List<String> ids =allChildrenIds.get(className);
        if(ids==null){
            ids=getAllChildren(className).stream().map(OwlClassV2::getName).toList();
            allChildrenIds.put(className,ids);
        }
        return ids;

    }

    public List<OwlClassV2> getAllChildren(OntClass.Named ontClass) {
        List<OwlClassV2> children = new ArrayList<>();
        // 拿到未包含推理的原始基础模型，用来判断真实的显式声明
        var baseModel = model.asInferenceModel().getRawModel();
        ontClass.subClasses(true)
                .filter(subClass -> {
                    // 如果开启了推理，MemberType 也会被当作子类返回。
                    // 此时我们去原始模型里看一眼：到底谁真正声明了 "subClassOf 当前父类"
                    boolean hasExplicitStatement = baseModel.contains(
                            baseModel.createStatement(
                                    baseModel.getResource(subClass.getURI()),
                                    RDFS.subClassOf,
                                    baseModel.getResource(ontClass.asNamed().getURI())
                            )
                    );
                    // 只有显式声明了父子关系的类（如 M_009）才放行
                    boolean isEquivalent = subClass.equivalentClasses().findAny().isPresent();
                    return !isEquivalent || hasExplicitStatement;
                })
                .forEach(subClass -> {
                    OwlClassV2 child = OwlClassV2Tree.builder()
                            .name(subClass.getLocalName())
                            .label(subClass.getLabel())
                            .children(new ArrayList<>())
                            .build();
                    subClass.equivalentClasses();
                    children.add(child);
                    children.addAll(buildOwlClassV2Tree(subClass));
                });
        return children;
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

    private List<OwlClassV2Tree> getChildren(OntClass ontClass) {
        List<OwlClassV2Tree> children = new ArrayList<>();

        ontClass.subClasses().forEach(subClass -> {
            OwlClassV2Tree child = OwlClassV2Tree.builder()
                    .name(subClass.getLocalName())
                    .label(subClass.getLabel())
                    .build();
            child.setChildren(new ArrayList<>());
            if (subClass.subClasses().findAny().isPresent()) {
                System.out.println(subClass.getLabel() + "--" + subClass.subClasses().toList().size());
                child.getChildren().addAll(buildOwlClassV2Tree(subClass));
            }

            children.add(child);
        });
        return children;
    }

    private List<OwlClassV2Tree> buildOwlClassV2Tree(OntClass ontClass) {
        List<OwlClassV2Tree> children = new ArrayList<>();
        // 拿到未包含推理的原始基础模型，用来判断真实的显式声明
        var baseModel = ontClass.getModel().asInferenceModel().getRawModel();
        ontClass.subClasses(true)
//                .filter(subClass->{
//                    var baseSubclass = baseModel.getResource(subClass.getURI());
//                    var baseOntClass = baseModel.getResource(ontClass.getURI());
//                    return baseSubclass.;
//                })
                .filter(subClass -> !subClass.getLocalName().equals("Nothing"))
                .filter(subClass -> {
                    // 如果开启了推理，MemberType 也会被当作子类返回。
                    // 此时我们去原始模型里看一眼：到底谁真正声明了 "subClassOf 当前父类"
                    boolean hasExplicitStatement = baseModel.contains(
                            baseModel.createStatement(
                                    baseModel.getResource(subClass.getURI()),
                                    RDFS.subClassOf,
                                    baseModel.getResource(ontClass.asNamed().getURI())
                            )
                    );
//                    System.out.println(subClass.equivalentClasses().count() + "hasExplicitStatement = " + hasExplicitStatement + "--" + subClass.getLabel() + "--" + ontClass.getLabel());
                    // 只有显式声明了父子关系的类（如 M_009）才放行
                    boolean isEquivalent = subClass.equivalentClasses().findAny().isPresent();
                    return !isEquivalent || hasExplicitStatement;
                })
                .forEach(subClass -> {
//                    System.out.println(subClass.getLabel() + "--" + subClass.findHasKey());
                    OwlClassV2Tree child = OwlClassV2Tree.builder()
                            .name(subClass.getLocalName())
                            .label(subClass.getLabel())
                            .equivalentClasses(subClass.equivalentClasses().map(OntClass::getLocalName).toList())
                            .isEquivalentClassesDirect(subClass.equivalentClasses().anyMatch(s->isDirectSubClass(ontClass,s)))
                            .children(new ArrayList<>())
                            .build();
                    subClass.equivalentClasses();
                    child.setProperties(this.getProperties(subClass.asNamed()));
                    child.getChildren().addAll(buildOwlClassV2Tree(subClass));
                    children.add(child);
                });
        return children;
    }

    private void buildOwlClassV2Tree(OwlClassV2Tree owlClassV2Tree, OntClass ontClass) {
        List<OwlClassV2Tree> children = new ArrayList<>();
//        System.out.println(ontClass.getLabel() + "--" + ontClass.subClasses().toList().size());
        ontClass.subClasses(true)
                .filter(cls->cls.getNameSpace().startsWith(this.ns))
                .forEach(subClass -> {
            OwlClassV2Tree child = OwlClassV2Tree.builder()
                    .name(subClass.getLocalName())
                    .label(subClass.getLabel())
                    .direct(isDirectSubClass(ontClass, subClass))
                    .equivalentClasses(subClass.equivalentClasses().map(OntClass::getLocalName).toList())
                    .isEquivalentClassesDirect(owlClassV2Tree.getEquivalentClasses().stream().anyMatch(s->isDirectSubClass(model.getOntClass(this.ns + s),subClass)))
                    .build();
                    children.add(child);
            buildOwlClassV2Tree(child, subClass);
        });
        owlClassV2Tree.setChildren(children);
        owlClassV2Tree.setProperties(this.getProperties(ontClass.asNamed()));
    }

    private List<OwlPropertyV2> getProperties(OntClass.Named ontClass) {
        // 2. 获取自定义的 Annotation Property 引用

        List<OwlPropertyV2> annotations = model.annotationProperties().map(prop -> OwlPropertyV2.builder()
                .name(prop.getLocalName())
                .label(prop.getLabel())
                .type(OwlPropertyTypeEnum.ANNOTATION)
                .ranges(ontClass.getProperty(prop) != null ? List.of(ontClass.getProperty(prop).getLiteral().getValue()) : null)
                .build()
        ).collect(Collectors.toList());
//        System.out.println(ontClass.getLocalName() + "--" + ontClass.getLabel() + getAllRangeProperties(ontClass).stream().map(m->m.getLocalName()).collect(Collectors.toSet()));

//        ontClass.properties().toList().forEach(property -> {
////            System.out.println(ontClass.getLabel() + "--" + property.getLabel() + property.getLocalName() + "---" + property.declaringClasses(true).map(m->m.getLocalName()).collect(Collectors.toSet()));
//        });
        List<OwlPropertyV2> propertyV2s = ontClass.properties().map(prop -> OwlPropertyV2.builder()
                        .name(prop.getLocalName())
                        .label(prop.getLabel())
                        .type(prop.canAs(OntObjectProperty.class) ? OwlPropertyTypeEnum.OBJECT : OwlPropertyTypeEnum.DATA)
                        .ranges(prop.ranges().filter(rang -> isDirectDefine(rang, prop)).map(Resource::getLocalName).toList())
                        .domains(prop.declaringClasses(true).map(Resource::getLocalName).collect(Collectors.toList()))
                        .direct(prop.declaringClasses(true).anyMatch(a -> a.getLocalName().equals(ontClass.getLocalName())))
                        .uiType(extractUiType(prop))
                        .build()
                )
                .collect(Collectors.toList()); // 使用 collect 显式归集


//        List<OwlPropertyV2> propertyV2s = ontClass.properties()
//                .map(prop -> OwlPropertyV2.builder()
//                        .name(prop.getLocalName())
//                        .label(prop.getLabel())
//                        .type(prop.canAs(OntObjectProperty.class) ? OwlPropertyTypeEnum.OBJECT : OwlPropertyTypeEnum.DATA)
//                        .ranges(prop.ranges().map(Resource::getLocalName).toList())
//                        .uiType(extractUiType(prop)) // 干净的单行调用，编译器绝不会报错
//                        .build()
//                )
//                .collect(Collectors.toList());

        propertyV2s.addAll(annotations);
        return propertyV2s;

    }

    // 抽取出来的私有辅助方法
    private String extractUiType(OntProperty prop) {
        var stmtIter = prop.listProperties();
        while (stmtIter.hasNext()) {
            var stmt = stmtIter.next();
            if ("uiType".equals(stmt.getPredicate().getLocalName())) {
                return stmt.getObject().asLiteral().getString();
            }
        }
        return null;
    }

    private boolean isDirectSubClass(OntClass parent, OntClass child) {
        var baseModel = model.asInferenceModel().getRawModel();
        // 如果开启了推理，MemberType 也会被当作子类返回。
        // 此时我们去原始模型里看一眼：到底谁真正声明了 "subClassOf 当前父类"
        boolean hasExplicitStatement = baseModel.contains(
                baseModel.createStatement(
                        baseModel.getResource(child.getURI()),
                        RDFS.subClassOf,
                        baseModel.getResource(parent.getURI())
                )
        );
//                    System.out.println(subClass.equivalentClasses().count() + "hasExplicitStatement = " + hasExplicitStatement + "--" + subClass.getLabel() + "--" + ontClass.getLabel());
        // 只有显式声明了父子关系的类（如 M_009）才放行
//        boolean isEquivalent = subClass.equivalentClasses().findAny().isPresent();
//        return !isEquivalent || hasExplicitStatement;
        return hasExplicitStatement;
    }

    private boolean isDirectDefine(Resource rang, OntProperty ontProp) {
        var baseModel = model.asInferenceModel().getRawModel();
        // 如果开启了推理，MemberType 也会被当作子类返回。
        // 此时我们去原始模型里看一眼：到底谁真正声明了 "subClassOf 当前父类"
        boolean hasExplicitStatement = baseModel.contains(
                baseModel.createStatement(
                        baseModel.getResource(ontProp.getURI()),
                        RDFS.range,
                        baseModel.getResource(rang.getURI())
                )
        );
//                    System.out.println(subClass.equivalentClasses().count() + "hasExplicitStatement = " + hasExplicitStatement + "--" + subClass.getLabel() + "--" + ontClass.getLabel());
        // 只有显式声明了父子关系的类（如 M_009）才放行
//        boolean isEquivalent = subClass.equivalentClasses().findAny().isPresent();
//        return !isEquivalent || hasExplicitStatement;
        return hasExplicitStatement;
    }

    private List<OntProperty> getAllRangeProperties(
            OntClass clazz) {

        List<OntProperty> props = new ArrayList<>();

        StmtIterator it = model.listStatements(
                null,
                RDFS.range,
                clazz
        );

        while (it.hasNext()) {

            Statement stmt = it.next();

            Resource propRes = stmt.getSubject();

            if (propRes.canAs(OntProperty.class)) {
                props.add(propRes.as(OntProperty.class));
            }
        }

        return props;
    }
}
