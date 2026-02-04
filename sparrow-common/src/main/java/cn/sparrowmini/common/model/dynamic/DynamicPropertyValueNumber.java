package cn.sparrowmini.common.model.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.math.BigDecimal;

@MappedSuperclass
public abstract class DynamicPropertyValueNumber<DT ,ID> extends  DynamicPropertyValue<BigDecimal,ID> {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "businessId", insertable = false, updatable = false)
    private DT businessObject;
}
