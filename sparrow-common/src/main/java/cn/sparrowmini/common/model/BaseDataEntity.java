package cn.sparrowmini.common.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


/**
 * 基础资料的基本数据结构
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseDataEntity extends BaseEntity {
    private String name;
    @Id
    private String code;
    private String content;
    private String type;
}
