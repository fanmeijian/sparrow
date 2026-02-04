package cn.sparrowmini.common.model.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DynamicPropertyValueString<DT ,ID> extends  DynamicPropertyValue<String,ID> {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "businessId", insertable = false, updatable = false)
    private DT businessObject;
}
