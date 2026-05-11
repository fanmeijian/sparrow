package cn.sparrowmini.owl.dto;

import cn.sparrowmini.owl.model.OwlPropertyTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for {@link cn.sparrowmini.owl.model.OwlProperty}
 */
@Data
@NoArgsConstructor
public class OwlPropertyDto implements Serializable {
    String parentId;
    String name;
    String code;
    String catalogId;
    String description;
    BigDecimal seq;
    Map<String, Object> labels;
    OwlPropertyTypeEnum type;
    String range;

    public OwlPropertyDto(String name, String code, String range, OwlPropertyTypeEnum type) {
        this.name = name;
        this.code = code;
        this.range = range;
        this.type = type;
    }
}