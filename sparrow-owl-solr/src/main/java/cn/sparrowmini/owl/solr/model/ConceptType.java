package cn.sparrowmini.owl.solr.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.solr.client.solrj.beans.Field;

import java.util.Collection;

@Setter
@Getter
public class ConceptType extends BaseMetadataObject {
    @Field("doctype")
    private String type = "class";

    @Field("topConceptOf")
    private Collection<String> topConceptOf;

    @Field("broader")
    private Collection<String> broader;

    @Field("inScheme")
    private Collection<String> inScheme;

    @Field("memberOf")
    private Collection<String> memberOf;
}
