package cn.sparrowmini.common.model.dynamic;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class DynamicPropertyValueSplitListener {
    @PreUpdate
    @PrePersist
    public void preSave(DynamicPropertyValueSplit<?,?> entity) {
        Object v = entity.getValue();
        DynamicPropertyTypeEnum type= entity.getDynamicProperty().getType();
        switch (type) {
            case String-> entity.getStringValue().add(new DynamicPropertyValueString((String) v));
            case Integer -> entity.getIntValue().add(new DynamicPropertyValueInteger((Integer) v));
        }

    }
}
