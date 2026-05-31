package cn.sparrowmini.owl.solr.model;


import java.util.ArrayList;
import java.util.List;


public interface IPropertyType extends IConcept {
    String COLLECTION = "props";
    String TYPE_FIELD = "doctype";
    String TYPE_VALUE = "property";
    String IS_FACET_FIELD = "isFacet";
    String IS_VISIBLE_FIELD = "isVisible";
    String IS_REQUIRED_FIELD = "isRequired";
    String BOOST_FIELD = "boost";
    String RANGE_FIELD = "range";
    String VALUE_QUALIFIER_FIELD = "valueQualifier";
    String USED_WITH_FIELD = "used_in";
    String USED_BY_FIELD = "used_by";
    String IDX_FIELD_NAME_FIELD = "idxField";
    String PROPERTY_TYPE_FIELD = "propType";
    String CODE_LIST_FIELD = "codeList";
    String CODE_LIST_ID_FIELD = "codeListId";

    static String[] defaultFieldNames() {
        return new String[]{"doctype", "isFacet", "isVisible", "boost", "idxField", "propType", "*_label", "*_alternate", "*_hidden", "languages", "*_txt", "localName", "nameSpace", "id", "*_comment", "*_desc", "range", "valueQualifier", "codeList", "codeListId"};
    }

    static List<Field> defaultFields() {
        List<Field> f = new ArrayList();

        for(String s : defaultFieldNames()) {
            f.add(new SimpleField(s));
        }

        return f;
    }
}
