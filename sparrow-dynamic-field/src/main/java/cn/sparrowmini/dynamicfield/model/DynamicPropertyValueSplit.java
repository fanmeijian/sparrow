package cn.sparrowmini.dynamicfield.model;

import cn.sparrowmini.common.model.BaseState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * 将每种不同类型的属性拆表使用，有点复杂，尽量不要用
 * @param <T>
 * @param <ID>
 */

@EntityListeners(DynamicPropertyValueSplitListener.class)
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class DynamicPropertyValueSplit<T, ID> extends BaseState {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected String id;

    @ElementCollection
    private List<DynamicPropertyValueString> stringValue = new ArrayList<>();

    @ElementCollection
    private List<DynamicPropertyValueInteger> intValue = new ArrayList<>();

    @Transient
    private Object value;

    private ID businessId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "businessId", insertable = false, updatable = false)
    private T businessObject;

    @Embedded
    private DynamicPropertyId dynamicPropertyId;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "entityType", insertable = false, updatable = false),
            @JoinColumn(name = "propertyKey", insertable = false, updatable = false)
    })
    private DynamicProperty dynamicProperty;


    public DynamicPropertyValueSplit(ID businessId, String propertyKey, Object value, Class<? extends DynamicProperty> clazz) {
        this.businessId = businessId;
        DiscriminatorValue dv = clazz.getAnnotation(DiscriminatorValue.class);
        if (dv == null) {
            throw new IllegalStateException("实体类 " + clazz.getSimpleName() + " 缺少 @DiscriminatorValue 注解");
        }
        String entityType = dv.value();
        this.dynamicPropertyId = new DynamicPropertyId(entityType,propertyKey);
        this.value = value;
    }

}