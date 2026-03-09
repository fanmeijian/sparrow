package cn.sparrowmini.common.model.dynamic;

import cn.sparrowmini.common.model.BaseState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.OffsetDateTime;


/**
 * 简化版的动态属性，
 * @param <T> 实体类型
 * @param <ID> 实体的ID类型
 *
 * @Entity
 * @Table(name = TablePrefix.NAME + "article_property_value")
 * public class ArticlePropertyValue extends DynamicPropertyValue<Article,String> {
 *
 *     @JsonIgnore
 *     @ManyToOne
 *     @JoinColumn(name = "businessId", insertable = false, updatable = false)
 *     private Article article;
 *
 *     public ArticlePropertyValue(String businessId, Object value, DynamicProperty articleProperty) {
 *         super(businessId,value, articleProperty);
 *
 *     }
 *
 * }
 *
 *     @OneToMany(mappedBy = "businessObject", cascade = CascadeType.REMOVE)
 *     private List<ArticlePropertyValue> propertyValues;
 */
@EntityListeners(DynamicPropertyValueListener.class)
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class DynamicPropertyValue<T, ID> extends BaseState {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected String id;

    @JsonIgnore
    private String stringValue;
    @JsonIgnore
    private Integer intValue;
    @JsonIgnore
    private Boolean booleanValue;
    @JsonIgnore
    private OffsetDateTime dateValue;

    @Column(name = "propertyKey", insertable = false, updatable = false)
    private String key;

    @JsonProperty("name")
    private String getName(){
        return dynamicProperty.getName();
    }

    @Transient
    private Object value;

    public Object getValue() {
        if(value == null){
            DynamicPropertyTypeEnum type= dynamicProperty.getType();
            switch (type) {
                case String-> {
                    return  this.stringValue;
                }
                case Integer -> {
                    return   this.intValue;
                }
                case Date -> {
                    return   this.dateValue;
                }
            }
        }
        return value;
    }

    private ID businessId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "businessId", insertable = false, updatable = false)
    private T businessObject;

//    @JsonIgnore
    @Embedded
    private DynamicPropertyId dynamicPropertyId;

    @JsonIgnore
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "entityType", insertable = false, updatable = false),
            @JoinColumn(name = "propertyKey", insertable = false, updatable = false)
    })
    private DynamicProperty dynamicProperty;


    public DynamicPropertyValue(ID businessId, Object value, DynamicProperty dynamicProperty) {
        this.businessId = businessId;
        Class<?> clazz = dynamicProperty.getClass();
        DiscriminatorValue dv = clazz.getAnnotation(DiscriminatorValue.class);
        if (dv == null) {
            throw new IllegalStateException("实体类 " + clazz.getSimpleName() + " 缺少 @DiscriminatorValue 注解");
        }
        this.dynamicProperty = dynamicProperty;
        String entityType = dv.value();
        String propertyKey = dynamicProperty.getPropertyKey();
        this.dynamicPropertyId = new DynamicPropertyId(entityType, propertyKey);
        this.value = value;
    }

}