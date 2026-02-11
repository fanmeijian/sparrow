package cn.sparrowmini.common.dto;

import cn.sparrowmini.common.model.CommonStateEnum;

import java.time.OffsetDateTime;

/**
 * Projection for {@link SimpleDictDto}
 */
public interface SimpleDictDtoInfo {
    OffsetDateTime getCreatedDate();

    String getCreatedBy();

    String getStat();

    CommonStateEnum getEntityStat();

    Boolean isEnabled();

    String getId();

    String getEntityType();

    String getName();

    String getCode();
}