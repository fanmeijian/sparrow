package cn.sparrowmini.dynamicfield.model;

import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

@MappedSuperclass
public abstract class DynamicPropertyValueBoolean implements Serializable {
    private Boolean value;

    public DynamicPropertyValueBoolean(Boolean value) {
        this.value = value;
    }
}
