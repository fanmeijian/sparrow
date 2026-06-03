package cn.sparrowmini.owl.solr.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class SkosRestriction extends Restriction implements Serializable {

    @Field("broader")
    private String broader;

    @Field("scheme")
    private String scheme;

    public SkosRestriction(String onProperty, String onClass, Boolean isRequired, String broader) {
        super(onProperty, onClass, isRequired);
        this.broader = broader;
    }

    public SkosRestriction(String onProperty, String onClass, Boolean isRequired, String broader, String scheme) {
        super(onProperty, onClass, isRequired);
        this.broader = broader;
        this.scheme = scheme;
    }
}
