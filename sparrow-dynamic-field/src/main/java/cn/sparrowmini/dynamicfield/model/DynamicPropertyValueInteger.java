package cn.sparrowmini.dynamicfield.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
@Embeddable
public class DynamicPropertyValueInteger implements Serializable {
    private Integer value;

    public DynamicPropertyValueInteger(Integer value) {
        this.value = value;
    }
}
