package cn.sparrowmini.owl.dto;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for {@link cn.sparrowmini.common.model.owl.OwlClass}
 */
@Value
public class OwlClassDto implements Serializable {
    String id;
    String parentId;
    String name;
    String code;
    String catalogId;
    String description;
    BigDecimal seq;
    Map<String, Object> labels;
}