package cn.sparrowmini.common.model.dynamic;

import cn.sparrowmini.common.model.BaseState;
import jakarta.persistence.*;
import lombok.*;
import org.glassfish.jaxb.core.v2.model.core.ID;

import java.io.Serializable;
@Getter
@Setter
@MappedSuperclass
public abstract class DynamicPropertyValue<T,ID> extends BaseState {
    private T value;

    @EmbeddedId
    private DynamicPropertyValueId<ID> id = new DynamicPropertyValueId<>();

    public DynamicPropertyValue(){

    }

    public DynamicPropertyValue(String propertyKey, ID businessId, T value){
        this.id = new DynamicPropertyValueId<>(propertyKey, businessId);
        this.value = value;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    public static class DynamicPropertyValueId<ID> implements Serializable {
        private String propertyKey; // 属性的唯一标识，如 "age", "color"
        private ID businessId;

        public DynamicPropertyValueId(String propertyKey, ID businessId) {
            this.propertyKey = propertyKey;
            this.businessId = businessId;
        }
    }
}