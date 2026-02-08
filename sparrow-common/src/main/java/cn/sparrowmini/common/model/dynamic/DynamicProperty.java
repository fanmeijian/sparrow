package cn.sparrowmini.common.model.dynamic;

import cn.sparrowmini.common.model.BaseState;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用于设置某个实体的动态属性列表
 * @NoArgsConstructor
 * @Entity
 * @DiscriminatorValue("PRODUCT")
 * public class ProductProperty extends DynamicProperty {
 *
 *     public ProductProperty(String test, DynamicPropertyTypeEnum dynamicPropertyTypeEnum) {
 *         super(test, dynamicPropertyTypeEnum);
 *     }
 *
 * }
 *
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "dynamic_property", uniqueConstraints = @UniqueConstraint(columnNames = {"entityType","propertyKey"}))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@IdClass(DynamicPropertyId.class)
public class DynamicProperty extends BaseState implements Serializable {
//    @Id
//    @GeneratedValue
//    @UuidGenerator
//    protected String id;

    @Id
    private String entityType;

    @Id
    private String propertyKey; // 属性的唯一标识，如 "age", "color"


    private String name;

    @Enumerated(EnumType.STRING)
    private DynamicPropertyTypeEnum type;
    private int seq;

    @Enumerated(EnumType.STRING)
    private DynamicPropertyValueProviderType providerType;


    private String url;

    @ElementCollection
    private List<ProviderData> providerData;

    @Lob
    private String providerScript;

    @Transient
    private List<ProviderDataValue> providerData_;

    public List<ProviderDataValue> getProviderData_() {
        if(providerType == null){
            return new ArrayList<>();
        }
        List<ProviderData> list = new ArrayList<>();
        Map<String, Object> vars = new HashMap<>();
        // 将 ProviderData 的 Class 对象传进去，脚本里可以直接用
        vars.put("ProviderData", cn.sparrowmini.common.model.dynamic.DynamicProperty.ProviderData.class);
        switch (providerType) {
            case SCRIPT:
                list = (List<ProviderData>) MVEL.eval(providerScript, vars);
                break;

            default: list = providerData;
            break;
        }
        List<ProviderDataValue> list2 = List.of();
        if(type.equals(DynamicPropertyTypeEnum.Integer)){
            list2 = list.stream().map(m->new ProviderDataValue(m.label,Integer.parseInt(m.value))).collect(Collectors.toList());

        }else{
            list2=list.stream().map(m->new ProviderDataValue(m.label,m.value)).collect(Collectors.toList());
        }
        return list2;
    }


    public DynamicProperty(String propertyKey, DynamicPropertyTypeEnum type) {
        this.propertyKey = propertyKey;
        this.type = type;
    }

    public String getEntityType() {
        if (this.entityType == null) {
            // 获取当前实例类上的 DiscriminatorValue 注解
            DiscriminatorValue dv = this.getClass().getAnnotation(DiscriminatorValue.class);
            if (dv != null) {
                return dv.value();
            }
        }
        return entityType;
    }

    @Data
    @Embeddable
    public static class ProviderData implements Serializable {
        private String label;
        private String value;
    }

    @PrePersist
    public void prePersist() {
        if (this.entityType == null) {
            DiscriminatorValue dv =
                    this.getClass().getAnnotation(DiscriminatorValue.class);
            if (dv != null) {
                this.entityType = dv.value();
            }
        }
    }

}