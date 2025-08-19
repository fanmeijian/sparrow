package cn.sparrowmini.common.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


/**
 * 基础资料的基本数据结构
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseDataUuidEntity extends BaseUuidEntity {
    private String name;
    private String code;
    private String content;
    private String type;
}
