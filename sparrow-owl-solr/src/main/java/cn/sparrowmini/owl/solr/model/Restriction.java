package cn.sparrowmini.owl.solr.model;

import cn.sparrowmini.owl.solr.service.SolrIdGenerator;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;
import java.util.Collection;

@NoArgsConstructor
@SuperBuilder
@Data
public class Restriction implements Serializable {
    @Field("doctype")
    @Builder.Default
    private String doctype = "restriction";

    @Field("id")
    private String id;

    @Field("onProperty")
    private String onProperty;

    @Field("onClass")
    private String onClass;

    @Field("onAllClass")
    private Collection<String> onAllClass;

    @Field("isRequired")
    private Boolean isRequired;

    @Field("valueProperty")
    private String valueProperty;

    @Field("valueRange")
    private Collection<String> value;
//
//    public Restriction(String onProperty, Collection<String> onClass, Boolean isRequired) {
//        this.onProperty = onProperty;
//        this.onClass = onClass;
//        this.isRequired = isRequired;
//        this.id = SolrIdGenerator.generateRestrictionId(String.join(",",onClass), onProperty);
//    }
}
