package cn.sparrowmini.common.dto;

import cn.sparrowmini.common.model.BaseOpLog;
import lombok.Value;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * DTO for {@link BaseOpLog}
 */
@Value
public class BaseOpLogDto implements Serializable {
    OffsetDateTime createdDate;
    OffsetDateTime modifiedDate;
    String createdBy;
    String modifiedBy;
}