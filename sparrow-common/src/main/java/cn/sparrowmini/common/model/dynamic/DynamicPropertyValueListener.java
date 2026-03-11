package cn.sparrowmini.common.model.dynamic;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class DynamicPropertyValueListener {
    @PreUpdate
    @PrePersist
    public void preSave(DynamicPropertyValue<?,?> entity) {
        Object v = entity.getValue();
        DynamicPropertyTypeEnum type= entity.getDynamicProperty().getType();
        switch (type) {
            case String-> entity.setStringValue((String) v);
            case Integer -> entity.setIntValue((Integer) v);
            case Boolean -> entity.setBooleanValue((Boolean) v);
            case Date -> entity.setDateValue(OffsetDateTime.parse(v.toString()));
        }

        if(entity.getDynamicProperty().getProviderType().equals(DynamicPropertyValueProviderType.DICT)){
            entity.setDictCode(entity.getStringValue());
        }
    }
}
