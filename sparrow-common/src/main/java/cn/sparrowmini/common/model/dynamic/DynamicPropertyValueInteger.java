package cn.sparrowmini.common.model.dynamic;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class DynamicPropertyValueInteger implements Serializable {
    private Integer value;
}
