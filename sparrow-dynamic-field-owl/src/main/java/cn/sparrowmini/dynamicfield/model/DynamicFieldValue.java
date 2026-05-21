package cn.sparrowmini.dynamicfield.model;

import cn.sparrowmini.common.model.BaseState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;


/**
 * 简化版的动态属性，
 *
 * @param <T>  实体类型
 * @param <ID> 实体的ID类型
 * @Entity
 * @Table(name = TablePrefix.NAME + "article_property_value")
 * public class ArticlePropertyValue extends DynamicPropertyValue<Article,String> {
 * @JsonIgnore
 * @ManyToOne
 * @JoinColumn(name = "businessId", insertable = false, updatable = false)
 * private Article article;
 * <p>
 * public ArticlePropertyValue(String businessId, Object value, DynamicProperty articleProperty) {
 * super(businessId,value, articleProperty);
 * <p>
 * }
 * <p>
 * }
 * @OneToMany(mappedBy = "businessObject", cascade = CascadeType.REMOVE)
 * private List<ArticlePropertyValue> propertyValues;
 */
@EntityListeners(DynamicFieldValueListener.class)
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class DynamicFieldValue<T, ID> extends BaseState {

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

    private String key;

    @Enumerated(EnumType.STRING)
    private DynamicFieldTypeEnum type;

    @Transient
    private Object value;


    public Object getValue() {
        if (value == null) {
            switch (type) {
                case String -> {
                    return this.stringValue;
                }
                case Integer -> {
                    return this.intValue;
                }
                case Date -> {
                    return this.dateValue;
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

    public DynamicFieldValue(ID businessId, String key, DynamicFieldTypeEnum type, Object value) {
        this.businessId = businessId;
        this.key = key;
        this.type = type;
        this.value = value;
    }


}