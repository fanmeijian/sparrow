package cn.sparrow.model.group;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.springframework.data.domain.Persistable;

import cn.sparrow.model.common.AbstractOperationLog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "spr_group_role")
public class GroupRole extends AbstractOperationLog implements Persistable<GroupRolePK> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  @EmbeddedId
  private GroupRolePK id;
  private String stat;

  public GroupRole() {

  }

  public GroupRole(GroupRolePK f) {
    this.id = f;
  }

  @Override
  public boolean isNew() {
    return true;
  }

}