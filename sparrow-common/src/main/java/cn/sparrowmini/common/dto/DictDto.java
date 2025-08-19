package cn.sparrowmini.common.dto;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link cn.sparrowmini.common.model.Dict}
 */
@Value
public class DictDto implements Serializable {
    String id;
    String name;
    String code;
    BigDecimal seq;
    String catalogId;
}