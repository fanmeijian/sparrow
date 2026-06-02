package cn.sparrowmini.owl.solr.service;

import cn.sparrowmini.owl.solr.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.*;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OntologyIndexService {

    private final SolrClient solrClient;
    private final OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_INF);

    private static final String COLLECTION_NAME = "class";

    private final Map<String, Set<String>> propertyUsageMap = new HashMap<>();

    public OntologyIndexService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public static void initCollections(SolrClient solrClient) {
        List<String> requiredCollections = Arrays.asList("props", "codes", "class", "item", "party","concepts");

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
        Set<String> nss = Set.of("http://www.nimble-project.org/catalogue#", "http://www.aidimme.es/FurnitureSectorOntology.owl#", "http://www.cn-plc.com/ontology/cms#");

        try (InputStream in = getClass().getResourceAsStream(ontologyPath)) {
            model.read(in, "RDF/XML");
            List<PropertyType> indexedProp = new ArrayList<>();

            List<OntClass.Named> indexedOntClass = model.classes().toList();
            AtomicLong count2 = new AtomicLong(indexedOntClass.size());
            indexedOntClass.forEach(ontologyClass -> {
                ClassType classType = processClazz(model, ontologyClass, indexedProp);
                System.out.println(count2.getAndDecrement() + "Indexed class: " + classType.getUri());
                if (classType != null) {
                    try {
                        solrClient.addBean(COLLECTION_NAME, classType);

                    } catch (IOException | SolrServerException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            solrClient.commit(COLLECTION_NAME);


            List<OntProperty> indexedOntProp = model.properties().toList();
            AtomicLong count1 = new AtomicLong(indexedOntProp.size());
            indexedOntProp.stream()
                    .filter(f -> nss.contains(f.getNameSpace()))
                    .forEach(property -> {
                        PropertyType prop = processProperty(model, property);

                        System.out.println(count1.getAndDecrement() + "Indexed property: " + property.getURI());
                        try {
                            solrClient.addBean("props", prop);

                        } catch (IOException | SolrServerException e) {
                            throw new RuntimeException(e);
                        }
                    });
            solrClient.commit("props");

            initOuterCodeList();
            indexAllSkosConcepts();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initOuterCodeList(){
        String ns = "http://www.cn-plc.com/ontology/cms#";
        Map<String,String> codeList = Map.of(
                "PartyStandardList","party.json",
                "TeamStandardList","team.json",
                "GlobalGeographyScheme","country.json",
                "RegionList","region_cn.json",
//                "YearList","year.json",
//                "MonthList","month.json",
                "LanguageList","lang.json");
        codeList.forEach((k,v)->{
//            initCodeList(ns +k + "Id",v);
            initOuterScheme(k ,v);
        });
    }

    public void indexAllSkosConcepts() {
        // 🔍 独立出一条完全不受 Class/Property 干扰的干净流水线
        model.individuals().forEach(ind -> {
            ConceptType conceptDoc = new ConceptType();
            String label = ind.getLabel();

            // 1. 基础元数据封装（继承自 BaseMetadataObject）
            conceptDoc.setUri(ind.getURI());
            conceptDoc.setLocalName(ind.getLocalName());
            conceptDoc.setNameSpace(ind.getNameSpace());
            conceptDoc.setLabel(Map.of("zh", label==null?"":label));
            conceptDoc.setLanguages(Set.of("zh"));

            // 2. 提取多语言 Label
            // ... 调用通用 setLabel / addLabel

            // 3. 🔍 核心 SKOS 图拓扑抽取
            // 抽取归属词表
            Statement schemeStmt = ind.getProperty(model.getProperty("http://www.w3.org/2004/02/skos/core#inScheme"));
            if (schemeStmt != null) {
                conceptDoc.setInScheme(schemeStmt.getObject().asResource().getURI());
            }

            // 抽取上位父级（用来做级联下探的铁链）
            Statement broaderStmt = ind.getProperty(model.getProperty("http://www.w3.org/2004/02/skos/core#broader"));
            if (broaderStmt != null) {
                conceptDoc.setBroader(broaderStmt.getObject().asResource().getURI());
                conceptDoc.setTopConcept(false);
            } else {
                // 没有父级，判定为第一级门户个体
                conceptDoc.setTopConcept(true);
            }

            // 4. 单向、纯净地推给 Solr
            try {
                solrClient.addBean("concepts", conceptDoc);
            } catch (IOException | SolrServerException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            solrClient.commit("concepts");
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initCodeList(String codeListId, String filePath){

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
                    codedType.setUri(String.join("#",ns,name));
                    codedType.setNameSpace(ns+ "#");
                    codedType.setLocalName(name);
                    codedType.setLabel(Map.of("zh", label));
                    codedType.setLanguages(Set.of("zh"));
                    codedType.setListId(listId);
                    codedType.setCode(name);

                    try {
                        solrClient.addBean("codes", codedType); // 💡 原本误将底层 Jena 的 item 传入，应传入 Solr 的实体 Bean：codedType
                        solrClient.commit("codes");
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

    private void initOuterScheme(String schemeName, String filePath){

        String ns = "http://www.cn-plc.com/ontology/cms#";

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

                    ConceptType conceptDoc = new ConceptType();

                    // 1. 基础元数据封装（继承自 BaseMetadataObject）
                    conceptDoc.setUri(ns + name);
                    conceptDoc.setLocalName(name);
                    conceptDoc.setNameSpace(ns);
                    conceptDoc.setLabel(Map.of("zh", label==null?"":label));
                    conceptDoc.setLanguages(Set.of("zh"));
                    conceptDoc.setInScheme(ns + schemeName);
                    // 2. 提取多语言 Label
                    // ... 调用通用 setLabel / addLabel


                    // 抽取上位父级（用来做级联下探的铁链）
                    if (schemeName.equals("RegionList")) {
                        conceptDoc.setBroader(ns + "CN");
                        conceptDoc.setTopConcept(false);
                        conceptDoc.setInScheme(ns + "GlobalGeographyScheme");
                    } else {
                        // 没有父级，判定为第一级门户个体
                        conceptDoc.setTopConcept(true);
                    }


                    try {
                        solrClient.addBean("concepts", conceptDoc); // 💡 原本误将底层 Jena 的 item 传入，应传入 Solr 的实体 Bean：codedType
                    } catch (Exception e) {
                        log.error("Failed to save item to Solr", e);
                    }

                    System.out.println("ID: " + label + ", Name: " + name);
                }
            }

            try {
                solrClient.commit("concepts");
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


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
        prop.ranges().findFirst().ifPresent(range -> index.setRange(range.getURI() != null ? range.getURI() : range.toString()));

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
            solrClient.commit("codes");
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
            index.setAllParents(clazz.superClasses().map(Resource::getURI).filter(Objects::nonNull).collect(Collectors.toList()));
            index.setParents(clazz.superClasses(true).map(Resource::getURI).filter(Objects::nonNull).collect(Collectors.toList()));
            index.setAllChildren(clazz.subClasses().map(Resource::getURI).filter(Objects::nonNull).collect(Collectors.toSet()));
            index.setChildren(clazz.subClasses(true).map(Resource::getURI).filter(Objects::nonNull).collect(Collectors.toSet()));
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
                    String propertyUri = superCls.as(OntClass.ValueRestriction.class).getProperty().getURI();
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
}