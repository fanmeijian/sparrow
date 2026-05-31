package cn.sparrowmini.owl.solr.model;

// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
import java.util.Collection;
import java.util.Map;

public interface IConcept {
    String SOLR_STRING = "string";
    String SOLR_NUMBER = "pdouble";
    String SOLR_INT = "pint";
    String SOLR_BOOLEAN = "boolean";
    String ID_FIELD = "id";
    String CODE_FIELD = "code";
    String LANGUAGES_FIELD = "languages";
    String LABEL_FIELD = "*_label";
    String LOWERCASE_LABEL_FIELD = "*_lowercaseLabel";
    String ALL_LABELS_FIELD = "allLabels";
    String LANGUAGE_ALL_LABELS_FIELD = "*_labels";
    String ALTERNATE_LABEL_FIELD = "*_alternate";
    String HIDDEN_LABEL_FIELD = "*_hidden";
    String LANGUAGE_TXT_FIELD = "*_txt";
    String TEXT_FIELD = "_text_";
    String COMMENT_FIELD = "*_comment";
    String NAME_SPACE_FIELD = "nameSpace";
    String LOCAL_NAME_FIELD = "localName";
    String DESCRIPTION_FIELD = "*_desc";
    String BASE_PLATFORM_FIELD = "basePlatform";

    Collection<String> getLanguages();

    Map<String, String> getLabel();

    Map<String, String> getComment();

    Map<String, String> getDescription();

    String getUri();

    String getCode();

    String getNameSpace();

    String getLocalName();
}
