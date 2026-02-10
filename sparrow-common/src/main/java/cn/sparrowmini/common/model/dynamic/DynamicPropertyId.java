package cn.sparrowmini.common.model.dynamic;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public final class DynamicPropertyId implements Serializable {
    private String entityType;  // 对应鉴别值列
    private String propertyKey; // 属性的唯一标识，如 "age", "color"

    public DynamicPropertyId(String entityType, String propertyKey) {
        this.entityType = entityType;
        this.propertyKey = propertyKey;
    }

    public DynamicPropertyId(String propertyKey) {
        this.propertyKey = propertyKey;
    }
}