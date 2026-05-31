package cn.sparrowmini.owl.solr.model;
// 引入 SolrJ 注解
import org.apache.solr.client.solrj.beans.Field;

public class CodedType extends Concept implements ICodedType {

    @Field("doctype")
    private String type = "code";

    @Field("codedList")
    private String listId;

    public CodedType() {
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getListId() {
        return this.listId;
    }

    public void setListId(String codedList) {
        this.listId = codedList;
    }
}