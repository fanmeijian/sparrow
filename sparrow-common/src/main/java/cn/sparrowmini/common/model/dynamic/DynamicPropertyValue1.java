package cn.sparrowmini.common.model.dynamic;

import cn.sparrowmini.common.model.BaseState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@MappedSuperclass
public abstract class DynamicPropertyValue1<T,ID> extends BaseState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    private String propertyKey;

    @ElementCollection
    private List<DynamicPropertyValueString2> stringValue;

    @ElementCollection
    private List<DynamicPropertyValueInteger> intValue;


    private ID businessId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "businessId", insertable = false, updatable = false)
    private T businessObject;

    @Embedded
    private DynamicPropertyId  dynamicPropertyId;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "entityType", insertable = false, updatable = false),
            @JoinColumn(name = "propertyKey", insertable = false, updatable = false)
    })
    private DynamicProperty dynamicProperty;

}