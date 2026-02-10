package cn.sparrowmini.common.model.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

@MappedSuperclass
public abstract class DynamicPropertyValueBoolean implements Serializable {
    private Boolean value;

    public DynamicPropertyValueBoolean(Boolean value) {
        this.value = value;
    }
}
