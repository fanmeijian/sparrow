package cn.sparrowmini.owl;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

@NoArgsConstructor
@Data
public class Restriction implements Serializable {
    private String doctype = "restriction";

    private String id;

    private String onProperty;

    private String onClass;

    private String valueProperty;

    private Collection<String> value;

    private Boolean isRequired = true;

    public Restriction(String onProperty, String onClass, Boolean isRequired) {
        this.onProperty = onProperty;
        this.onClass = onClass;
        this.isRequired = isRequired;
    }
}
