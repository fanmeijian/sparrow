package cn.sparrowmini.common.model.pem.group;

import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 群组本身的定义
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "group", uniqueConstraints = {@UniqueConstraint(columnNames = {"dtype","code"})})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public class Group extends BaseUuidEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private String owner;
    private Boolean isRoot;

}
