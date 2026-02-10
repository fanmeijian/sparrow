package cn.sparrowmini.common.model.dynamic;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class DynamicPropertyValueListener {
    @PreUpdate
    @PrePersist
    public void preSave(DynamicPropertyValue<?,?> entity) {
        Object v = entity.getValue();
        DynamicPropertyTypeEnum type= entity.getDynamicProperty().getType();
        switch (type) {
            case String-> entity.setStringValue((String) v);
            case Integer -> entity.setIntValue((Integer) v);
        }
//        entity.getIntValue().add(new DynamicPropertyValueInteger((Integer) v));
    }
}
