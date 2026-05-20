package cn.sparrowmini.dynamicfield.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
@Embeddable
public class DynamicPropertyValueString implements Serializable {
    private String value;

    public DynamicPropertyValueString(String value) {
        this.value = value;
    }
}
