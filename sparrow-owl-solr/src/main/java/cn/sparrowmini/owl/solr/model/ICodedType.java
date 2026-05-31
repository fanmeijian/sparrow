package cn.sparrowmini.owl.solr.model;

public interface ICodedType extends IConcept {
    String COLLECTION = "codes";
    String TYPE_FIELD = "doctype";
    String TYPE_VALUE = "code";
    String LIST_ID_FIELD = "codedList";
}
