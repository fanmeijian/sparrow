package cn.sparrowmini.common.model.dynamic;

import jakarta.persistence.*;
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
