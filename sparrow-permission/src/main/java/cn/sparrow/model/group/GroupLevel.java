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
@Table(name = "spr_group_level")
public class GroupLevel extends AbstractOperationLog implements Persistable<GroupLevelPK> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  @EmbeddedId
  private GroupLevelPK id;
  private String stat;

  public GroupLevel() {

  }

  public GroupLevel(GroupLevelPK f) {
    this.id = f;
  }

  @Override
  public boolean isNew() {
    return true;
  }

}