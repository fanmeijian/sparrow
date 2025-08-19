package cn.sparrowmini.common.dto;

import cn.sparrowmini.common.model.BaseState;
import cn.sparrowmini.common.model.CommonStateEnum;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link BaseState}
 */
@Value
public class BaseStateDto implements Serializable {
    String stat;
    CommonStateEnum entityStat;
    Boolean enabled;
    Boolean hidden;
}