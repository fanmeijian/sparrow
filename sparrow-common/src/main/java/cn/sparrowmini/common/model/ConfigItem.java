package cn.sparrowmini.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Constructor;

/**
 * 单行配置
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "config_item", uniqueConstraints = @UniqueConstraint(columnNames = {"dtype","code"}))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public class ConfigItem extends BaseUuidEntity {
    private String code;
    private String name;
    private String catalogId;

    @Transient
    private Object value;

    private String valueStr;
    private String valueType;

    private String remark;

    public Object getValue() {
        if (valueStr == null || valueType == null) return null;

        try {
            Class<?> clazz = Class.forName(valueType);

            // 1. 如果类型本身就是 String，直接返回
            if (clazz == String.class) return valueStr;

            // 2. 核心逻辑：获取该类型接收 String 参数的构造方法
            // 例如 Integer(String s), Boolean(String s)
            Constructor<?> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(valueStr);

        } catch (NoSuchMethodException e) {
            // 如果没有 String 构造函数（比如基本类型 int, 需转为包装类 Integer）
            return handlePrimitiveTypes(valueType, valueStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 辅助方法：处理没有 String 构造函数的特殊类型或基本类型
     */
    private Object handlePrimitiveTypes(String typeStr, String val) {
        return switch (typeStr) {
            case "int", "java.lang.Integer" -> Integer.parseInt(val);
            case "boolean", "java.lang.Boolean" -> Boolean.parseBoolean(val);
            case "long", "java.lang.Long" -> Long.parseLong(val);
            case "double", "java.lang.Double" -> Double.parseDouble(val);
            default -> val;
        };
    }

    @PrePersist
    @PreUpdate
    public void preSave(){
        this.valueStr = this.value==null?"":this.value.toString();
    }
}
