package cn.sparrowmini.common.dto;

import cn.sparrowmini.common.model.CommonStateEnum;
import lombok.Value;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * DTO for {@link cn.sparrowmini.common.model.SimpleDict}
 */
@Value
public class SimpleDictDto implements Serializable {
    OffsetDateTime createdDate;
    String createdBy;
    String stat;
    CommonStateEnum entityStat;
    Boolean enabled;
    String id;
    String entityType;
    String name;
    String code;
}