package cn.sparrowmini.owl.solr.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.solr.client.solrj.beans.Field;

@Setter
@Getter
public class ConceptType extends BaseMetadataObject {
    @Field("isTopConcept")
    private boolean topConcept;

    @Field("broader")
    private String broader;
    @Field("inScheme")
    private String inScheme;
}
