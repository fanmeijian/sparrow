package cn.sparrowmini.owl.solr.model;
// 引入 SolrJ 注解
import lombok.Getter;
import lombok.Setter;
import org.apache.solr.client.solrj.beans.Field;

@Setter
@Getter
public class CodedType extends BaseMetadataObject implements ICodedType {

    @Field("doctype")
    private String type = "code";

    @Field("codedList")
    private String listId;

    public CodedType() {
    }

}