package cn.sparrowmini.owl.solr.service;

import cn.sparrowmini.owl.solr.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.*;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OntologyIndexService {
    Set<String> filterClass = Set.of("http://www.w3.org/2002/07/owl#", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    Set<String> nss = Set.of("http://www.cn-plc.com/ontology/cms#");

    private final SolrClient solrClient;
//    private final OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_INF);

    private final OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF);
    private static final String COLLECTION_NAME = "class";

    private final Map<String, Set<String>> propertyUsageMap = new HashMap<>();

    public OntologyIndexService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public static void initCollections(SolrClient solrClient) {
        List<String> requiredCollections = Arrays.asList("props", "class", "item", "party", "concepts");

        for (String collection : requiredCollections) {
            try {
                // 1. Get the list of currently existing collections in the cluster
                List<String> existing = CollectionAdminRequest.listCollections(solrClient);

                // 2. If it doesn't exist, create it
                if (!existing.contains(collection)) {
                    // Parameters: (Collection Name, ConfigSet, NumShards, ReplicationFactor)
                    CollectionAdminRequest.Create createRequest =
                            CollectionAdminRequest.createCollection(collection, "_default", 1, 1);

                    // CORRECT WAY: Call .process() on the request object itself
                    createRequest.process(solrClient);

                    System.out.println("Collection created successfully: " + collection);
                }
            } catch (Exception e) {
                System.err.println("Failed to initialize collection: " + collection + " -> " + e.getMessage());
            }
        }
    }

    public void createIndex(String ontologyPath) {


        try (InputStream in = getClass().getResourceAsStream(ontologyPath)) {
            // 2. 让 Jena 像 Protégé 一样，把 SKOS 官方的元数据定义“塞”进去
            System.out.println("正在加载 SKOS 官方核心定义...");
            // 提示：如果远程获取较慢，建议把这个 URL 的内容下载到本地，改为读取本地文件
            model.read("http://www.w3.org/2004/02/skos/core", "RDF/XML");

            // 3. 接着读取你自己的业务 XML 数据文件
            System.out.println("正在加载业务 XML 数据...");

            model.read(in, "RDF/XML");
            List<PropertyType> indexedProp = new ArrayList<>();

            List<OntClass.Named> indexedOntClass = model.classes().toList();
            AtomicLong count2 = new AtomicLong(indexedOntClass.size());
            indexedOntClass
                    .stream().filter(ont -> nss.contains(ont.getNameSpace()))
                    .forEach(ontologyClass -> {
                        ClassType classType = processClazz(model, ontologyClass, indexedProp);

                        System.out.println(count2.getAndDecrement() + "Indexed class: " + classType.getUri());
                        try {
                            solrClient.addBean(COLLECTION_NAME, classType);
                            //定义属性的restriction
//                            Restriction broaderRestriction = extractBroaderUri(classType.getUri());
//                            if (broaderRestriction != null) {
//                                solrClient.addBean("props", broaderRestriction);
//                            }
//
//                            Restriction schemeRestriction = extractSchemeUri(classType.getUri());
//                            if (schemeRestriction != null) {
//                                solrClient.addBean("props", schemeRestriction);
//                            }

                        } catch (IOException | SolrServerException e) {
                            throw new RuntimeException(e);
                        }
                    });
//            solrClient.commit(COLLECTION_NAME);


            List<OntProperty> indexedOntProp = model.properties().toList();
            AtomicLong count1 = new AtomicLong(indexedOntProp.size());
            indexedOntProp.stream()
                    .filter(f -> f.getNameSpace() != null && nss.contains(f.getNameSpace()))
                    .forEach(property -> {
                        PropertyType prop = processProperty(model, property);

                        System.out.println(count1.getAndDecrement() + "Indexed property: " + property.getURI());
                        try {
                            solrClient.addBean("props", prop);

                        } catch (IOException | SolrServerException e) {
                            throw new RuntimeException(e);
                        }
                    });


            model.ontObjects(OntClass.UnaryRestriction.class) // 返回 Stream<OntClass.UnaryRestriction<?, ?>>
                    .filter(f -> f.isLocal() && f.subClasses().findAny().isPresent())
                    .forEach(r -> {
                        Restriction restriction = OwlHelper.getRestrictionOfProperty(r);
                        restriction.setId(SolrIdGenerator.generateRestrictionId(String.join(",",restriction.getOnClass()), restriction.getOnProperty()));
                        try {
                            solrClient.addBean("props", restriction);
                        } catch (IOException | SolrServerException e) {
                            throw new RuntimeException(e);
                        }
                    });

//            solrClient.commit("props");

//            initOuterCodeList();
            indexAllSkosConcepts();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initOuterCodeList() {
        String ns = "http://www.cn-plc.com/ontology/cms#";
        List<OuterCodes> outerCodes = new ArrayList<>();
        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "PartyScheme"))
                .file("party_other_power.json")
                .collection(Set.of(ns + "PowerPartyCollection"))
                .topSchemeOf(Set.of(ns + "PartyScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "PartyScheme"))
                .file("party_sgcc.json")
                .collection(Set.of(ns + "SgccCollection"))
                .topSchemeOf(Set.of(ns + "PartyScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "PartyScheme"))
                .file("party_csg.json")
                .collection(Set.of(ns + "CsgCollection"))
                .topSchemeOf(Set.of(ns + "PartyScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "PartyScheme"))
                .file("party_association.json")
                .collection(Set.of(ns + "AssociationPartyCollection"))
                .topSchemeOf(Set.of(ns + "PartyScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "PartyScheme"))
                .file("party_inspect.json")
                .collection(Set.of(ns + "InspectPartyCollection"))
                .topSchemeOf(Set.of(ns + "PartyScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "GeographyScheme"))
                .file("country.json")
                .topSchemeOf(Set.of(ns + "GeographyScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "GeographyScheme"))
                .file("region_cn.json")
                .broader(Set.of(ns + "CN"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "LanguageScheme"))
                .file("lang.json")
                .topSchemeOf(Set.of(ns + "LanguageScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "MonthScheme"))
                .file("month.json")
                .topSchemeOf(Set.of(ns + "MonthScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "YearScheme"))
                .file("year.json")
                .topSchemeOf(Set.of(ns + "YearScheme"))
                .build());

        outerCodes.add(OuterCodes.builder()
                .inScheme(Set.of(ns + "PartyScheme"))
                .file("party_international_standard.json")
                .collection(Set.of(ns + "InternationalPartyCollection"))
                .topSchemeOf(Set.of(ns + "PartyScheme"))
                .build());


        outerCodes.forEach(this::initOuterScheme);
    }

    public void indexAllSkosConcepts() {
        // 🔍 独立出一条完全不受 Class/Property 干扰的干净流水线
        model.individuals().forEach(ind -> {
            ConceptType conceptDoc = new ConceptType();
            String label = ind.getLabel();

            // 1. 获取个体的【直接主要类】（代替旧版的 getOntClass()）
            Optional<OntClass> directClassOpt = ind.classes(true).findFirst();

            if (directClassOpt.isPresent()) {
                OntClass mainClass = directClassOpt.get();
                if (!mainClass.isAnon()) {
                    String mainClassUri = mainClass.getURI();
                    System.out.println("直接所属类 URI: " + mainClassUri);
                    conceptDoc.setType(mainClass.getLocalName());
                }
            }

            // 1. 基础元数据封装（继承自 BaseMetadataObject）
            conceptDoc.setUri(ind.getURI());
            conceptDoc.setLocalName(ind.getLocalName());
            conceptDoc.setNameSpace(ind.getNameSpace());
            conceptDoc.setLabel(Map.of("zh", label == null ? "" : label));
            conceptDoc.setLanguages(Set.of("zh"));

            // 2. 提取多语言 Label
            // ... 调用通用 setLabel / addLabel

            // 3. 🔍 核心 SKOS 图拓扑抽取
            // 抽取归属词表
            // 1. 初始化一个 Set 容器来收集所有的 Scheme URI
            Set<String> inSchemes = new HashSet<>();

            // 2. 核心改变：利用 listProperties 捞出 ind 身上所有的 skos:inScheme 边
            ind.listProperties(SKOS.inScheme).forEachRemaining(stmt -> {
                if (stmt.getObject().isResource()) {
                    String schemeURI = stmt.getObject().asResource().getURI();
                    if (schemeURI != null) {
                        inSchemes.add(schemeURI);
                    }
                }
            });

            // 3. 塞入你的 Solr Bean 中（你的 conceptDoc 对应的 setInScheme 应该接收 Collection/List）
            if (!inSchemes.isEmpty()) {
                conceptDoc.setInScheme(inSchemes);
            }

            // 抽取上位父级（用来做级联下探的铁链）
            Collection<String> boarders = new HashSet<>();
            ind.listProperties(SKOS.broader)
                    .filterKeep(stmt -> stmt.getObject().isResource()
                            && stmt.getObject().asResource().getURI() != null)
                    .forEachRemaining(stmt -> {
                        boarders.add(stmt.getObject().asResource().getURI());
                    });
            if (!boarders.isEmpty()) {
                conceptDoc.setBroader(boarders);
            }
            if (ind.getURI().equals("http://www.cn-plc.com/ontology/cms#TS_011")) {
                System.out.println("" + ind.listProperties().toList().size());
                ind.listProperties().forEachRemaining(stmt -> {
                    System.out.println(stmt.asTriple().toString());
                });
            }

            Set<String> topSchemeOfs = new HashSet<>();
            // 依赖高级推理：直接获取当前个体身上【包含由 hasTopConcept 推导而来】的所有顶级方案关系
            ind.listProperties(SKOS.topConceptOf)
                    .filterKeep(stmt -> stmt.getObject().isResource()
                            && stmt.getObject().asResource().getURI() != null)
                    .forEachRemaining(stmt -> {
                        topSchemeOfs.add(stmt.getObject().asResource().getURI());
                    });
            if (!topSchemeOfs.isEmpty()) {
                conceptDoc.setTopConceptOf(topSchemeOfs);
            }


            if (ind.getLocalName().equals("SgccCollection")) {
                System.out.println("");
            }

            //collection成员 memberOf
// 假设当前变量是 OntIndividual ind (你的概念或组织个体)
            Set<String> memberOfs = new HashSet<>();

// 核心：在模型中查询 ( ? , skos:member, ind )
            model.listStatements(null, SKOS.member, ind)
                    .forEachRemaining(stmt -> {
                        RDFNode collectionNode = stmt.getSubject(); // 拿到主语，即上层的 Collection
                        if (collectionNode.isURIResource()) {
                            String collectionUri = collectionNode.asResource().getURI();
                            if (collectionUri != null) {
                                memberOfs.add(collectionUri);
                            }
                        }
                    });

// 塞入你的 Solr Bean 中（一个成员可能属于多个 Collection，所以依然推荐多值存储）
            if (!memberOfs.isEmpty()) {
                conceptDoc.setMemberOf(memberOfs);
            }


//                // 1. 初始化一个 Set 自动去重
//                Set<String> topSchemeOfs = new HashSet<>();
//
//                // 【路径 A：逆向穿透】直接获取当前个体身上显式声明的 skos:topConceptOf 属性
//                Statement topConceptOfStmt = ind.getProperty(SKOS.topConceptOf);
//                if (topConceptOfStmt != null && topConceptOfStmt.getObject().isResource()) {
//                    topSchemeOfs.add(topConceptOfStmt.getObject().asResource().getURI());
//                }
//
//                // 【路径 B：正向穿透（Jena 5.6 兼容写法）】
//                // 既然个体没有 .relations()，我们直接在模型(Model)中寻找：谁以 SKOS.hasTopConcept 关联了当前个体
//                model.listStatements(null, SKOS.hasTopConcept, ind)
//                        .forEachRemaining(stmt -> {
//                            RDFNode schemeNode = stmt.getSubject(); // 拿到主语（即 Scheme）
//                            if (schemeNode.isURIResource()) {
//                                topSchemeOfs.add(schemeNode.asResource().getURI());
//                            }
//                        });
//
//                // 2. 将最终的集合塞进你的 conceptDoc 中
//                conceptDoc.setTopConceptOf(new ArrayList<>(topSchemeOfs));


            // 4. 单向、纯净地推给 Solr
            try {
                solrClient.addBean("concepts", conceptDoc);
            } catch (IOException | SolrServerException e) {
                throw new RuntimeException(e);
            }
        });

//        try {
//            solrClient.commit("concepts");
//        } catch (SolrServerException | IOException e) {
//            throw new RuntimeException(e);
//        }

    }

    private void initCodeList(String codeListId, String filePath) {

        String[] codeListIdFullName = codeListId.split("#");
        String ns = codeListIdFullName[0];
        String listId = codeListIdFullName[1];

        ObjectMapper objectMapper = new ObjectMapper();

        // 使用 Spring 的 ClassPathResource
        ClassPathResource resource = new ClassPathResource(filePath);

        try (InputStream inputStream = resource.getInputStream()) {
            // 1. 读取为 JsonNode
            JsonNode rootNode = objectMapper.readTree(inputStream);

            // 2. 判断是否为数组并遍历
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    String name = node.get("name").asText();
                    String label = node.get("label").asText();


                    CodedType codedType = new CodedType();
                    codedType.setUri(String.join("#", ns, name));
                    codedType.setNameSpace(ns + "#");
                    codedType.setLocalName(name);
                    codedType.setLabel(Map.of("zh", label));
                    codedType.setLanguages(Set.of("zh"));
                    codedType.setListId(listId);
                    codedType.setCode(name);

                    try {
                        solrClient.addBean("codes", codedType); // 💡 原本误将底层 Jena 的 item 传入，应传入 Solr 的实体 Bean：codedType
//                        solrClient.commit("codes");
                    } catch (Exception e) {
                        log.error("Failed to save item to Solr", e);
                    }

                    System.out.println("ID: " + label + ", Name: " + name);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initOuterScheme(OuterCodes codes) {

        String ns = "http://www.cn-plc.com/ontology/cms#";

        ObjectMapper objectMapper = new ObjectMapper();

        if(codes.file.equals("party_international_standard.json")) {
            System.out.println("");
        }

        // 使用 Spring 的 ClassPathResource
        ClassPathResource resource = new ClassPathResource(codes.getFile());

        try (InputStream inputStream = resource.getInputStream()) {
            // 1. 读取为 JsonNode
            JsonNode rootNode = objectMapper.readTree(inputStream);

            // 2. 判断是否为数组并遍历
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    String name = node.get("name").asText();
                    String label = node.get("label").asText();

                    ConceptType conceptDoc = new ConceptType();

                    // 1. 基础元数据封装（继承自 BaseMetadataObject）
                    conceptDoc.setType(SKOS.Concept.getLocalName());
                    conceptDoc.setUri(ns + name);
                    conceptDoc.setLocalName(name);
                    conceptDoc.setNameSpace(ns);
                    conceptDoc.setLabel(Map.of("zh", label == null ? "" : label));
                    conceptDoc.setLanguages(Set.of("zh"));
                    conceptDoc.setInScheme(codes.getInScheme());
                    if (codes.getBroader() != null) {
                        conceptDoc.setBroader(codes.getBroader());
                    }

                    conceptDoc.setTopConceptOf(codes.getTopSchemeOf());
                    conceptDoc.setMemberOf(codes.getCollection());


                    try {
                        solrClient.addBean("concepts", conceptDoc); // 💡 原本误将底层 Jena 的 item 传入，应传入 Solr 的实体 Bean：codedType
                    } catch (Exception e) {
                        log.error("Failed to save item to Solr", e);
                    }

                    System.out.println("ID: " + label + ", Name: " + name);
                }
            }

//            try {
//                solrClient.commit("concepts");
//            } catch (SolrServerException e) {
//                throw new RuntimeException(e);
//            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private Collection<String> getPropertyRange(OntProperty prop) {
        Collection<String> result = new ArrayList<>();
        RDFNode domainNode = prop.getPropertyResourceValue(RDFS.range);
        if (domainNode != null && domainNode.canAs(OntClass.class)) {
            Resource domainResource = domainNode.asResource().as(OntClass.class);
            if (domainResource.isAnon()) {
                System.out.println("=== domain ===");
                OntClass.UnionOf unionClass = domainResource.as(OntClass.UnionOf.class);

                System.out.println("「Jena 5.6 OntAPI 成功解析 Union Domain」：");
                result.addAll(unionClass.components().map(Resource::getURI).toList());
            }
        } else {
            result.addAll(prop.ranges().map(Resource::getURI).toList());
        }

        return result;
    }


    /**
     * Helper method to obtain all necessary information for indexing a property
     */
    private PropertyType processProperty(OntModel model, OntProperty prop) {
        PropertyType index = new PropertyType();
        index.setUri(prop.getURI());
        index.setLocalName(prop.getLocalName());
        index.setNameSpace(prop.getNameSpace());

        // Jena 5 推荐安全获取第一个 Range 的方法

        index.setRange(OwlHelper.getRangesOfProperty(prop));

        // 修正之前的 isVisible 和 isRequired 逻辑
        index.setVisible(NIMBLEOntology.isVisible(prop, true));
        index.setRequired(NIMBLEOntology.isRequired(prop, false));
        index.setFacet(prop.canAs(OntObjectProperty.class));

        ValueQualifier valueQualifier = getValueQualifier(prop);
        if (valueQualifier != null) {
            switch (valueQualifier) {
                case QUANTITY:
                    index.setValueQualifier(ValueQualifier.QUANTITY);
                    processCodedTypes(index, ValueQualifier.QUANTITY, prop);
                    break;
                case TEXT:
                case CODE:
                    index.setValueQualifier(ValueQualifier.TEXT);
                    processCodedTypes(index, ValueQualifier.TEXT, prop);
                    break;
                default:
                    index.setValueQualifier(valueQualifier);
            }
        }

        // ✨ 彻底修复：移除 .as(OntResource.class) 直接传入资源对象 (Resource)
        index.setLabel(obtainMultilingualValues(prop, RDFS.label, SKOS.prefLabel));
        index.setHiddenLabel(obtainMultilingualLabels(prop, SKOS.hiddenLabel));
        index.setAlternateLabel(obtainMultilingualLabels(prop, SKOS.altLabel));
        index.setComment(obtainMultilingualValues(prop, RDFS.comment, SKOS.definition));

        if (index.getLabel() != null) {
            for (String label : index.getLabel().values()) {
                index.addItemFieldName(ItemType.dynamicFieldPart(label));
            }
        }

        index.addItemFieldName(prop.getLocalName());
        index.addItemFieldName(ItemType.dynamicFieldPart(prop.getURI()));


        // 3. 🚀 奇迹时刻：直接从 Map 里以 O(1) 速度抓取直接使用了当前属性的分类
        Set<String> directClasses = new HashSet<>(propertyUsageMap.getOrDefault(prop.getURI(), Collections.emptySet()));

        //还要包含自身定义的匿名domain
        directClasses.addAll(this.getPropertyDomains(prop));

        // 5. 塞入 Solr DTO 丢给 props 集合
        index.getProduct().clear();
        index.getProduct().addAll(directClasses);


        List<String> types_ = new ArrayList<>();
        StmtIterator types = prop.listProperties();
        List<String> noStrings = List.of("Resource", "Property");
        long i = Iterators.count(types);
        types = prop.listProperties();

        while (types.hasNext()) {
            Statement stmt = types.nextStatement();
            if (stmt.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                    && stmt.getObject().isResource()
                    && !noStrings.contains(stmt.getResource().getLocalName())) {
                types_.add(stmt.getResource().getLocalName());
            }
        }

        if (!types_.isEmpty()) {
            index.setPropertyType(types_.size() > 1 ? types_.get(1) : types_.get(0));
        }

        return index;
    }

    private Set<String> getUsage(OntModel model, OntClass ontClass) {
        Set<String> classes = new HashSet<>();
        if (ontClass == null) {
            return classes;
        }
        classes.addAll(ontClass.properties().map(Resource::getURI).collect(Collectors.toSet()));
        classes.addAll(ontClass.superClasses().filter(f -> f.canAs(OntClass.ValueRestriction.class)).map(m -> m.as(OntClass.ValueRestriction.class).getProperty().getURI()).collect(Collectors.toSet()));
        return classes;
    }


    private Set<String> getSubClasses(OntClass cls) {
        return cls.subClasses().map(OntClass::getURI).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private ValueQualifier getValueQualifier(OntProperty prop) {
        if (NIMBLEOntology.isQuantityProperty(prop)) {
            return ValueQualifier.QUANTITY;
        }
        if (NIMBLEOntology.isCodeProperty(prop)) {
            return ValueQualifier.TEXT;
        }
        if (NIMBLEOntology.isFileProperty(prop)) {
            return ValueQualifier.FILE;
        }

        Optional<? extends Resource> rangeOpt = prop.ranges().findFirst();
        if (rangeOpt.isPresent()) {
            Resource range = rangeOpt.get();
            if (!range.isAnon() && range.getURI() != null) {
                if (range.getNameSpace().equals(XSD.NS)) {
                    return fromXSDLocalName(range.getLocalName());
                } else if (NIMBLEOntology.isUnitType(range)) {
                    return ValueQualifier.QUANTITY;
                } else if (NIMBLEOntology.isCodeType(range)) {
                    return ValueQualifier.TEXT;
                }
            }
        }
        return null;
    }

    private ValueQualifier fromXSDLocalName(String localName) {
        switch (localName) {
            case "float":
            case "double":
            case "decimal":
            case "int":
                return ValueQualifier.NUMBER;
            case "boolean":
                return ValueQualifier.BOOLEAN;
            case "string":
            case "normalizedString":
            default:
                return ValueQualifier.STRING;
        }
    }

    private void processCodedTypes(PropertyType pt, ValueQualifier qualifier, OntProperty resource) {
        Set<String> codeSet = new HashSet<>(pt.getCodeList());
        String codeListUri = null;

        Iterator<Statement> nimbleIter = NIMBLEOntology.listNimbleStatements(resource);
        while (nimbleIter.hasNext()) {
            Statement stmt = nimbleIter.next();
            if (stmt.getObject().isLiteral()) {
                codeSet.add(stmt.getObject().asLiteral().getString());
            } else if (stmt.getObject().isResource()) {
                Resource nRes = stmt.getObject().asResource();
                if (NIMBLEOntology.isListType(nRes)) {
                    codeListUri = NIMBLEOntology.listId(nRes, nRes.getURI());
                    codeSet.addAll(processCodedList(nRes));
                } else {
                    codeListUri = resource.getURI();
                    codeSet.add(processCodedItem(resource, nRes));
                }
            }
        }
        pt.getCodeList().addAll(codeSet);
        pt.setCodeListId(codeListUri);
    }

    private Set<String> processCodedList(Resource list) {
        Set<String> codes = new HashSet<>();
        Iterator<Statement> iter = NIMBLEOntology.listNimbleStatements(list);
        while (iter.hasNext()) {
            Statement stmt = iter.next();
            if (stmt.getObject().isLiteral()) {
                codes.add(stmt.getObject().asLiteral().getString());
            } else if (stmt.getObject().isResource()) {
                codes.add(processCodedItem(list, stmt.getObject().asResource()));
            }
        }
        return codes;
    }

    private String processCodedItem(Resource list, Resource item) {
        CodedType codedType = new CodedType();
        codedType.setUri(item.getURI());
        codedType.setNameSpace(item.getNameSpace());
        codedType.setLocalName(item.getLocalName());

        processLabels(codedType, item);
        codedType.setListId(NIMBLEOntology.listId(list, list.getURI()));
        codedType.setCode(NIMBLEOntology.hasCode(item, item.getLocalName()));

        try {
            solrClient.addBean("codes", codedType); // 💡 原本误将底层 Jena 的 item 传入，应传入 Solr 的实体 Bean：codedType
//            solrClient.commit("codes");
        } catch (Exception e) {
            log.error("Failed to save item to Solr", e);
        }
        return codedType.getCode();
    }

    private void processLabels(BaseMetadataObject concept, Resource resource) {
        concept.setLabel(obtainMultilingualValues(resource, RDFS.label, SKOS.prefLabel));
        concept.setAlternateLabel(obtainMultilingualLabels(resource, SKOS.altLabel));
        concept.setHiddenLabel(obtainMultilingualLabels(resource, SKOS.hiddenLabel));
        concept.setDescription(obtainMultilingualValues(resource, SKOS.definition));
        concept.setComment(obtainMultilingualValues(resource, RDFS.comment, SKOS.note));
    }

    private ClassType processClazz(OntModel model, OntClass clazz, List<PropertyType> availableProps) {
        if (!clazz.isAnon()) {
            ClassType index = new ClassType();
            index.setUri(clazz.getURI());
            index.setLocalName(clazz.getLocalName());
            index.setNameSpace(clazz.getNameSpace());

            // ✨ 彻底修复：直接传入 OntClass（它本身就是 Resource 顶层）
            index.setLabel(obtainMultilingualValues(clazz, RDFS.label, DC.title, SKOS.prefLabel));
            index.setDescription(obtainMultilingualValues(clazz, DC.description));
            index.setComment(obtainMultilingualValues(clazz, RDFS.comment, DC.description, SKOS.definition));
            index.setHiddenLabel(obtainMultilingualLabels(clazz, SKOS.hiddenLabel));
            index.setAlternateLabel(obtainMultilingualLabels(clazz, SKOS.altLabel));

            index.setProperties(getProperties(clazz, model));
            index.setAllParents(clazz.superClasses().filter(c->c.getNameSpace()!=null && nss.contains(c.getNameSpace())).map(Resource::getURI).filter(Objects::nonNull).collect(Collectors.toList()));
            index.setParents(clazz.superClasses(true).filter(c->c.getNameSpace()!=null &&nss.contains(c.getNameSpace())).map(Resource::getURI).filter(Objects::nonNull).collect(Collectors.toList()));
            index.setAllChildren(clazz.subClasses().filter(c->c.getNameSpace()!=null &&nss.contains(c.getNameSpace())).map(Resource::getURI).filter(Objects::nonNull).collect(Collectors.toSet()));
            index.setChildren(clazz.subClasses(true).filter(c->c.getNameSpace()!=null &&nss.contains(c.getNameSpace())).map(Resource::getURI).filter(Objects::nonNull).collect(Collectors.toSet()));
            return index;
        }
        return null;
    }

    /**
     * Helper method to extract multilingual hidden and alternate labels
     * 入参转换为通用基础 Resource 接口，支持所有本体类型
     */
    private Map<String, Collection<String>> obtainMultilingualLabels(Resource prop, Property... properties) {
        Map<String, Collection<String>> languageMap = new HashMap<>();
        for (Property property : properties) {
            // 使用通用的基础方法遍历
            StmtIterator sIter = prop.listProperties(property);
            while (sIter.hasNext()) {
                RDFNode node = sIter.nextStatement().getObject();
                if (node.isLiteral()) {
                    String lang = node.asLiteral().getLanguage();
                    languageMap.computeIfAbsent(lang, k -> new ArrayList<>()).add(node.asLiteral().getString());
                }
            }
        }
        return languageMap;
    }

    /**
     * Helper method to extract multilingual labels
     * 入参转换为通用基础 Resource 接口
     */
    private Map<String, String> obtainMultilingualValues(Resource prop, Property... properties) {
        Map<String, String> languageMap = new HashMap<>();
        for (Property property : properties) {
            StmtIterator sIter = prop.listProperties(property);
            while (sIter.hasNext()) {
                RDFNode node = sIter.nextStatement().getObject();
                if (node.isLiteral()) {
                    String lang = node.asLiteral().getLanguage();
                    languageMap.putIfAbsent(lang, node.asLiteral().getString());
                }
            }
        }
        return languageMap;
    }

    public SkosRestriction extractSchemeUri(String classUri) {
        String queryString =
                "PREFIX owl:  <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
                        "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        // 🌟 核心修复：在这里必须把 ?property 暴露出来
                        "SELECT ?property ?targetScheme WHERE { " +
                        "  ?classUri rdfs:subClassOf ?restriction1 . " +
                        "  ?restriction1 owl:onProperty ?property ; " +
                        "                owl:someValuesFrom ?restriction2 . " +
                        "  ?restriction2 owl:onProperty ?innerProp ; " +
                        "                owl:hasValue ?targetScheme . " +
                        "  ?targetScheme rdf:type skos:ConceptScheme . " +
                        "}";

        ParameterizedSparqlString pss = new ParameterizedSparqlString(queryString);
        pss.setIri("classUri", classUri);

        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                if (soln.contains("targetScheme") && soln.get("targetScheme") != null) {
                    // 🌟 投影补齐后，这里就能稳稳拿到属性实体，告别 NullPointerException
                    RDFNode propertyNode = soln.get("property");
                    String propUri = propertyNode.asResource().getURI();
                    String broaderUri = null;
                    String schemeUri = soln.get("targetScheme").asResource().getURI();

                    System.out.println("【Scheme提取成功】" + propUri + " -> " + schemeUri);
                    return SkosRestriction.builder()
                            .onProperty(propUri)
                            .onClass(classUri)
                            .isRequired(true)
                            .broader(broaderUri)
                            .scheme(schemeUri)
                            .id(SolrIdGenerator.generateRestrictionId(classUri, propUri))
                            .build();
                }
            }
        } catch (Exception e) {
            System.err.println("【Scheme提取失败】" + e.getMessage());
            e.printStackTrace(); // 开发阶段打印堆栈是个好习惯
        }
        return null;
    }

    public SkosRestriction extractBroaderUri(String classUri) {
        String queryString =
                "PREFIX owl:  <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
                        "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        // 🌟 修复核心：必须在这里加上 ?property，告诉解析器把它带出来！
                        "SELECT ?property ?broaderConcept WHERE { " +
                        "  ?classUri rdfs:subClassOf ?restriction1 . " +
                        "  ?restriction1 owl:onProperty ?property ; " +
                        "                owl:someValuesFrom ?restriction2 . " +
                        "  ?restriction2 owl:onProperty ?invBroader ; " +
                        "                owl:someValuesFrom ?oneOfClass . " +
                        "  ?invBroader   owl:inverseOf skos:broader . " +
                        "  ?oneOfClass   owl:oneOf ?list . " +
                        "  ?list         rdf:first ?broaderConcept . " +
                        "}";

        ParameterizedSparqlString pss = new ParameterizedSparqlString(queryString);
        pss.setIri("classUri", classUri);

        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();

                if (soln.contains("broaderConcept") && soln.get("broaderConcept") != null) {
                    RDFNode propertyNode = soln.get("property");
                    String propUri = propertyNode.asResource().getURI();
                    String broaderUri = soln.get("broaderConcept").asResource().getURI();
                    String schemeUri = null;
                    return SkosRestriction.builder()
                            .onProperty(propUri)
                            .onClass(classUri)
                            .isRequired(true)
                            .broader(broaderUri)
                            .scheme(schemeUri)
                            .id(SolrIdGenerator.generateRestrictionId(classUri, propUri))
                            .build();
                }
            }
        } catch (Exception e) {
            System.err.println("【broader提取失败】" + e.getMessage());
        }
        return null;
    }

    public SkosRestriction extractBroader(String classUri) {
        String queryString =
                "PREFIX owl:  <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
                        "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX cms:  <http://www.cn-plc.com/ontology/cms#> " +
                        "SELECT ?property ?targetScheme WHERE { " +
                        "  ?classUri rdfs:subClassOf ?restriction1 . " +
                        "  ?restriction1 owl:onProperty ?property ; " + // 别忘了末尾是分号
                        "                owl:someValuesFrom ?restriction2 . " +
                        "  ?restriction2 owl:onProperty ?innerProp ; " +
                        "                owl:hasValue ?targetScheme . " +
                        "  ?targetScheme rdf:type skos:ConceptScheme . " +
                        "}";

        ParameterizedSparqlString pss = new ParameterizedSparqlString(queryString);
        pss.setIri("classUri", classUri);

        Query query = pss.asQuery();
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                RDFNode propertyNode = soln.get("property");
                if (propertyNode == null) continue;

                String propUri = propertyNode.asResource().getURI();
                String broaderUri = null;
                String schemeUri = null;

                // 分支 1：命中区域的 broader 限制
                if (soln.contains("broaderConcept") && soln.get("broaderConcept") != null) {
                    broaderUri = soln.get("broaderConcept").asResource().getURI();
                    System.out.println("【SPARQL】严格命中[地区]限制 -> 绑定概念: " + broaderUri);
                }

                // 分支 2：过滤命中 Scheme 方案限制
                if (soln.contains("targetScheme") && soln.get("targetScheme") != null) {
                    schemeUri = soln.get("targetScheme").asResource().getURI();
                    System.out.println("【SPARQL】严格命中[国家/方案]限制 -> 绑定顶级方案: " + schemeUri);
                }

                if (broaderUri == null && schemeUri == null) {
                    continue;
                }

                return SkosRestriction.builder()
                        .onProperty(propUri)
                        .onClass(classUri)
                        .isRequired(true)
                        .broader(broaderUri)
                        .scheme(schemeUri)
                        .id(SolrIdGenerator.generateRestrictionId(classUri, propUri))
                        .build();
            }
        } catch (Exception e) {
            throw new RuntimeException("SKOS 属性深度严格检索失败", e);
        }
        return null;
    }

    private Set<String> getProperties(final OntClass ontClass, final OntModel model) {
        Set<String> properties = new HashSet<>();
        String classUri = ontClass.getURI();
        if (classUri == null) return properties;
        // 2. 扫描该类直接关联的属性 (rdfs:domain)

        ontClass.properties().forEach(prop -> {
            propertyUsageMap.computeIfAbsent(prop.getURI(), k -> new HashSet<>()).add(classUri);
            properties.add(prop.getURI());
        });

        // 3. ✨ 顺便扫描该类身上挂载的 Restriction 嵌套属性
        ontClass.superClasses(false)
                .filter(f -> f.canAs(OntClass.Restriction.class))
                .forEach(superCls -> {
                    String propertyUri = superCls.as(OntClass.UnaryRestriction.class).getProperty().getURI();
                    propertyUsageMap.computeIfAbsent(propertyUri, k -> new HashSet<>()).add(classUri);
                    properties.add(propertyUri);
                });

        return properties;
    }

    private Set<String> getSuperClasses(OntClass cls) {
        return cls.superClasses()
                .filter(f -> f.canAs(OntClass.class))
                .map(OntClass::getURI)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<String> getPropertyDomains(OntProperty ontProperty) {
        List<String> result = new ArrayList<>();
        RDFNode domainNode = ontProperty.getPropertyResourceValue(RDFS.domain);
        if (domainNode != null) {
            Resource domainResource = domainNode.asResource().as(OntClass.class);
            if (domainResource.isAnon()) {
                System.out.println("=== domain ===");
                OntClass.UnionOf unionClass = domainResource.as(OntClass.UnionOf.class);

                System.out.println("「Jena 5.6 OntAPI 成功解析 Union Domain」：");
                result.addAll(unionClass.components().map(Resource::getURI).toList());
            }
        } else {
            result.addAll(ontProperty.domains().map(Resource::getURI).toList());
        }

        return result;
    }


    @Builder
    @Data
    public static class OuterCodes {
        private String docType = "concept";
        private Collection<String> inScheme;
        private String file;
        private Collection<String> collection;
        private Collection<String> broader;
        private Collection<String> topSchemeOf;

    }
}