package cn.sparrowmini.common.model.pem.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link cn.sparrowmini.common.model.pem.Sysrole}
 */
@Value
public class SysroleDtoSimple implements Serializable {
    String id;
    String name;
    String code;
}