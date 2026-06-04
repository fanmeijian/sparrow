package cn.sparrowmini.owl.solr.model;

import cn.sparrowmini.owl.solr.service.SolrIdGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;
import java.util.Collection;

@NoArgsConstructor
@Data
public class Restriction implements Serializable {
    @Field("doctype")
    private String doctype = "restriction";

    @Field("id")
    private String id;

    @Field("onProperty")
    private String onProperty;

    @Field("onClass")
    private String onClass;

    @Field("isRequired")
    private Boolean isRequired;

    @Field("valueProperty")
    private String valueProperty;

    @Field("value")
    private Collection<String> value;

    public Restriction(String onProperty, String onClass, Boolean isRequired) {
        this.onProperty = onProperty;
        this.onClass = onClass;
        this.isRequired = isRequired;
        this.id = SolrIdGenerator.generateRestrictionId(onClass, onProperty);
    }
}
