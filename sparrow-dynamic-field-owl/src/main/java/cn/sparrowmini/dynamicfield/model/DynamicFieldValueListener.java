package cn.sparrowmini.dynamicfield.model;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.OffsetDateTime;

public class DynamicFieldValueListener {
    @PreUpdate
    @PrePersist
    public void preSave(DynamicFieldValue<?,?> entity) {
        Object v = entity.getValue();
        DynamicFieldTypeEnum type= entity.getType();
        switch (type) {
            case String-> entity.setStringValue((String) v);
            case Integer -> entity.setIntValue((Integer) v);
            case Boolean -> entity.setBooleanValue((Boolean) v);
            case Date -> entity.setDateValue(OffsetDateTime.parse(v.toString()));
        }
    }
}
