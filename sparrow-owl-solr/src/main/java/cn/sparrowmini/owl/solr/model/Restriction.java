package cn.sparrowmini.owl.solr.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class Restriction implements Serializable {
    @Field("doctype")
    private String doctype = "restriction";

    @Field("onProperty")
    private String onProperty;

    @Field("onClass")
    private String onClass;

    @Field("isRequired")
    private Boolean isRequired;

    public Restriction(String onProperty, String onClass, Boolean isRequired) {
        this.onProperty = onProperty;
        this.onClass = onClass;
        this.isRequired = isRequired;
    }
}
