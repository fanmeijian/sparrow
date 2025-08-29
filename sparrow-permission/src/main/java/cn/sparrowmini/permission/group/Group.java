package cn.sparrowmini.permission.group;

import java.io.Serializable;

import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "group")
public class Group extends BaseTree implements Serializable {

    private String owner;
}
