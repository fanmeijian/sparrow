package cn.sparrow.model.permission;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import cn.sparrow.model.common.AbstractOperationLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "spr_user_sysrole")
@EntityListeners(AuditingEntityListener.class)
public class UserSysrole extends AbstractOperationLog {
  private static final long serialVersionUID = 1L;

  @EqualsAndHashCode.Include
  @EmbeddedId
  private UserSysrolePK id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sysrole_id", insertable = false, updatable = false)
  private Sysrole sysrole;
  
//  @ManyToOne
//  @JoinColumn(name = "menu_id", insertable = false, updatable = false)
//  private Menu menu;
  
  public UserSysrole(UserSysrolePK id) {
    this.id = id;
  }

}
