package cn.sparrowmini.owl.solr.service;

import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NIMBLEOntology {
    public static final String NS = "http://www.nimble-project.org/catalogue#";
    public static final String QUANTITY_TYPE = "QuantityType";
    public static final String CODE_TYPE = "CodeType";
    public static final String UNIT_TYPE = "UnitType";
    public static final String LIST_TYPE = "ListType";
    public static final String UNIT_LIST = "UnitList";
    public static final String CODE_LIST = "CodeList";

    // property localName's
    public static final String HAS_CODE = "hasCode";
    public static final String HAS_UNIT = "hasUnit";
    public static final String HAS_CODE_LIST = "hasCodeList";
    public static final String HAS_UNIT_LIST = "hasUnitList";
    public static final String CODE = "code";
    private static final String UNIT_CODE = "unitCode";
    public static final String IS_VISIBLE = "isVisible";
    public static final String IS_REQUIRED = "isRequired";
    public static final String ID = "id";

    public static final String QUANTITY_PROPERTY_TYPE = "QuantityProperty";
    public static final String CODE_PROPERTY_TYPE = "CodeProperty";
    public static final String FILE_PROPERTY_TYPE = "FileProperty";

    private static final String ONT_FILE = "/NIMBLEOntology.owl";
    private static NIMBLEOntology instance;
    private final OntModel nimbleModel;

    private final Set<OntProperty> UNIT_LIST_PROPS = new HashSet<>();
    private final Set<OntProperty> CODE_LIST_PROPS = new HashSet<>();
    private final Set<OntProperty> CODE_PROPERTY_PROPS = new HashSet<>();
    private final Set<OntProperty> QUANTITY_PROPERTY_PROPS = new HashSet<>();


    private NIMBLEOntology() {
        nimbleModel = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_INF); //ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
//		codeTypeProperty = nimbleModel.createOntProperty(NS + CODE_PROPERTY_TYPE);
//		unitTypeProperty = nimbleModel.createOntProperty(NS + QUANTITY_PROPERTY_TYPE);
//		codeType = nimbleModel.createClass(NS + CODE_TYPE);
//		unitType = nimbleModel.createClass(NS + UNIT_TYPE);
//		unitListType = nimbleModel.createClass(NS + UNIT_LIST);
//		codeListType = nimbleModel.createClass(NS + CODE_LIST);
//
        try {
            load(nimbleModel, ONT_FILE, Lang.RDFXML);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        initProperties();
    }


    private void initProperties() {
        CODE_LIST_PROPS.add(getOntProperty(NS + HAS_CODE));
        CODE_LIST_PROPS.add(getOntProperty(NS + CODE));

        // UNIT list properties (hasCode, code, hasUnit)
        UNIT_LIST_PROPS.add(getOntProperty(NS + HAS_CODE));
        UNIT_LIST_PROPS.add(getOntProperty(NS + CODE));
        UNIT_LIST_PROPS.add(getOntProperty(NS + HAS_UNIT));

        // quantity property
        QUANTITY_PROPERTY_PROPS.add(getOntProperty(NS + HAS_CODE));
        QUANTITY_PROPERTY_PROPS.add(getOntProperty(NS + HAS_CODE_LIST));
        QUANTITY_PROPERTY_PROPS.add(getOntProperty(NS + HAS_UNIT));
        QUANTITY_PROPERTY_PROPS.add(getOntProperty(NS + HAS_UNIT_LIST));
        QUANTITY_PROPERTY_PROPS.add(getOntProperty(NS + CODE));
        QUANTITY_PROPERTY_PROPS.add(getOntProperty(NS + UNIT_CODE));

        // code property elements
        CODE_PROPERTY_PROPS.add(getOntProperty(NS + HAS_CODE));
        CODE_PROPERTY_PROPS.add(getOntProperty(NS + HAS_CODE_LIST));
        CODE_PROPERTY_PROPS.add(getOntProperty(NS + CODE));
    }

    private static NIMBLEOntology getInstance() {
        if (instance == null) {
            instance = new NIMBLEOntology();
        }
        return instance;
    }

    private void load(OntModel model, String name, Lang lang) throws IOException {
        try (InputStream inStream = getClass().getResourceAsStream(name)) {
            if (inStream != null) {
                RDFParser.create()
                        .source(inStream)
                        .errorHandler(ErrorHandlerFactory.errorHandlerStrict)
                        .lang(lang)
                        .base("http://www.nimble-project.eu/onto/")
                        .parse(model);
            }
        }
    }

    public static boolean isQuantityProperty(OntProperty resource) {
        return getInstance().checkUnitTypeProperty(resource);
    }

    private boolean checkUnitTypeProperty(OntProperty resource) {
        if (resource == null) return false;
        OntProperty unitTypeProperty = getOntProperty(NS + QUANTITY_PROPERTY_TYPE);
        return resource.hasURI(unitTypeProperty.getURI()) || resource.hasSuperProperty(unitTypeProperty, false);
    }

    public static boolean isFileProperty(OntProperty resource) {
        return getInstance().checkFileTypeProperty(resource);
    }

    private boolean checkFileTypeProperty(OntProperty resource) {
        if (resource == null) return false;
        OntProperty unitTypeProperty = getOntProperty(NS + FILE_PROPERTY_TYPE);
        return resource.hasURI(unitTypeProperty.getURI()) || resource.hasSuperProperty(unitTypeProperty, false);
    }

    public static boolean isCodeProperty(OntProperty resource) {
        return getInstance().checkCodeTypeProperty(resource);
    }

    private boolean checkCodeTypeProperty(OntProperty resource) {
        if (resource == null) return false;
        System.out.println("checkCodeTypeProperty" + resource.getURI());
        OntProperty codeTypeProperty = getOntProperty(NS + CODE_PROPERTY_TYPE);
        return resource.hasURI(codeTypeProperty.getURI()) || resource.hasSuperProperty(codeTypeProperty, false);
    }

    // ✨ 彻底修复：将所有旧版 OntResource 的入参改写为通用基础 Resource 接口
    public static boolean isCodeList(Resource resource) {
        return getInstance().checkCodeList(resource);
    }

    private boolean checkCodeList(Resource resource) {
        if (resource == null || resource.getURI() == null) return false;
        OntClass codeList = getOntClass(NS + CODE_LIST);
        return resource.hasURI(codeList.getURI()) || resource.hasProperty(RDF.type, codeList);
    }

    public static boolean isListType(Resource resource) {
        return isUnitList(resource) || isCodeList(resource);
    }

    public static boolean isUnitList(Resource resource) {
        return getInstance().checkUnitList(resource);
    }

    private boolean checkUnitList(Resource resource) {
        if (resource == null || resource.getURI() == null) return false;
        OntClass codeList = getOntClass(NS + UNIT_LIST);
        return resource.hasURI(codeList.getURI()) || resource.hasProperty(RDF.type, codeList);
    }

    public static boolean isCodeType(Resource resource) {
        return getInstance().checkCodeType(resource);
    }

    private boolean checkCodeType(Resource resource) {
        if (resource == null || resource.getURI() == null) return false;
        OntClass codeList = getOntClass(NS + CODE_TYPE);
        return resource.hasURI(codeList.getURI()) || resource.hasProperty(RDF.type, codeList);
    }

    public static boolean isUnitType(Resource resource) {
        return getInstance().checkUnitType(resource);
    }

    private boolean checkUnitType(Resource resource) {
        if (resource == null || resource.getURI() == null) return false;
        OntClass codeList = getOntClass(NS + UNIT_TYPE);
        return resource.hasURI(codeList.getURI()) || resource.hasProperty(RDF.type, codeList);
    }

    private OntClass getOntClass(String uri) {
        // ✨ 彻底修复：使用 Jena 5 标准的流式组件检索模型中的命名 Class
        OntClass cls = nimbleModel.classes()
                .filter(c -> uri.equals(c.getURI()))
                .findFirst()
                .orElse(null);

        if (cls == null) {
            // 如果不存在，使用 OWL 2 配置安全创建
            cls = nimbleModel.createOntClass(uri);
        }
        return cls;
    }


    private OntProperty getOntProperty(String uri) {
        // 1. 依然优先从原模型中找
        OntProperty prop = nimbleModel.properties()
                .filter(p -> uri.equals(p.getURI()))
                .findFirst()
                .orElse(null);

        if (prop == null) {
            // 2. 找不到时，在【临时模型】里创建！
            // 这样返回的对象拥有完整的 OntProperty 行为，但原模型 nimbleModel 滴水不漏，完全没被污染。
            prop = nimbleModel.createObjectProperty(uri);
        }
        return prop;
    }

    // ✨ 彻底修复：返回原生的 Statement 迭代器
    public static Iterator<Statement> listNimbleStatements(Resource resource) {
        return getInstance().obtainNimbleStatements(resource).iterator();
    }

    private Set<Statement> obtainNimbleStatements(Resource resource) {
        Set<Statement> statements = new HashSet<>();
        for (OntProperty prop : getNimbleProperties(resource)) {
            if (prop != null) {
                // ✨ 彻底修复：显式将其转换为基础的 Property 对象传入
                Property baseProp = prop.asProperty();

                StmtIterator iter = resource.listProperties(baseProp);
                while (iter.hasNext()) {
                    statements.add(iter.nextStatement());
                }
            }
        }
        return statements;
    }

    private Set<OntProperty> getNimbleProperties(Resource forResource) {
        if (forResource == null) {
            return new HashSet<>();
        }

        if (checkCodeList(forResource)) {
            return getProperties(NS + CODE_LIST);
        } else if (checkUnitList(forResource)) {
            return getProperties(NS + UNIT_LIST);
        } else if (checkCodeType(forResource)) {
            return getProperties(NS + CODE_TYPE);
        } else if (checkUnitType(forResource)) {
            return getProperties(NS + UNIT_TYPE);
        }

        // 判断属性的子类继承关系
        if (forResource.canAs(OntProperty.class)) {
            OntProperty ontProp = forResource.as(OntProperty.class);
            if (checkUnitTypeProperty(ontProp)) {
                return getProperties(NS + QUANTITY_PROPERTY_TYPE);
            } else if (checkCodeTypeProperty(ontProp)) {
                return getProperties(NS + CODE_PROPERTY_TYPE);
            }
        }

        return new HashSet<>();
    }

    private Set<OntProperty> getProperties(String resourceUri) {
        // 利用截取 LocalName 的方式安全做映射
        String localName = resourceUri.substring(resourceUri.indexOf("#") + 1);
        switch (localName) {
            case CODE_LIST:
                return CODE_LIST_PROPS;
            case UNIT_LIST:
                return UNIT_LIST_PROPS;
            case CODE_PROPERTY_TYPE:
                return CODE_PROPERTY_PROPS;
            case QUANTITY_PROPERTY_TYPE:
                return QUANTITY_PROPERTY_PROPS;
            default:
                return new HashSet<>();
        }
    }

    // ✨ 彻底修复：采用通用的 getProperty 检索字面量值，不再依赖 getPropertyValue()
    public static String hasCode(Resource resource, String def) {
        if (resource == null) return def;

        // ✨ 彻底修复：追加 .asProperty() 转换为原生 Property 接口
        Property hasCodeProp = getInstance().getOntProperty(NS + HAS_CODE).asProperty();
        Statement stmt = resource.getProperty(hasCodeProp);
        if (stmt != null && stmt.getObject().isLiteral()) {
            return stmt.getObject().asLiteral().getString();
        }

        // ✨ 彻底修复：追加 .asProperty() 转换为原生 Property 接口
        Property codeProp = getInstance().getOntProperty(NS + CODE).asProperty();
        stmt = resource.getProperty(codeProp);
        if (stmt != null && stmt.getObject().isLiteral()) {
            return stmt.getObject().asLiteral().getString();
        }
        return def;
    }

    public static boolean isVisible(OntProperty resource, boolean def) {
        if (resource == null) return def;
        // ✨ 彻底修复：追加 .asProperty()
        Property visibleProp = getInstance().getOntProperty(NS + IS_VISIBLE).asProperty();
        if (visibleProp != null) {
            Statement stmt = resource.getProperty(visibleProp);
            if (stmt != null && stmt.getObject().isLiteral()) {
                return stmt.getObject().asLiteral().getBoolean();
            }
        }
        return def;
    }

    public static boolean isRequired(OntProperty resource, boolean def) {
        if (resource == null) return def;
        // ✨ 彻底修复：追加 .asProperty()
        Property requiredProp = getInstance().getOntProperty(NS + IS_REQUIRED).asProperty();
        if (requiredProp != null) {
            Statement stmt = resource.getProperty(requiredProp);
            if (stmt != null && stmt.getObject().isLiteral()) {
                return stmt.getObject().asLiteral().getBoolean();
            }
        }
        return def;
    }

    public static String listId(Resource resource, String def) {
        if (resource == null) return def;
        // ✨ 彻底修复：追加 .asProperty()
        Property idProp = getInstance().getOntProperty(NS + ID).asProperty();
        Statement stmt = resource.getProperty(idProp);
        if (stmt != null && stmt.getObject().isLiteral()) {
            return stmt.getObject().asLiteral().getString();
        }
        return def;
    }

    public static void main(String[] args) {
        Set<OntProperty> aList = getInstance().getProperties(NS + QUANTITY_PROPERTY_TYPE);
        Set<OntProperty> aList2 = getInstance().getProperties(NS + CODE_PROPERTY_TYPE);
    }
}