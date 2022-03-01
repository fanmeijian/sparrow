package cn.sparrow.model.organization;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.sparrow.model.common.AbstractOperationLog;
import cn.sparrow.permission.listener.RepositoryErrorFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "spr_organization_relation")
public class OrganizationRelation extends AbstractOperationLog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @EqualsAndHashCode.Include
  @EmbeddedId
  private OrganizationRelationPK id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "organization_id", insertable = false, updatable = false)
  private Organization organization;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  private Organization parent;

  public OrganizationRelation(OrganizationRelationPK id) {
    this.id = id;
  }

  public OrganizationRelation(Organization f) {
    this.organization = f;
  }

  @PrePersist
  @PreUpdate
  private void preSave() {
    if (id.getOrganizationId().equals(id.getParentId())) {
      throw new RepositoryConstraintViolationException(
          RepositoryErrorFactory.getErros(this, "", "can not add relation to self"));
    }
  }

}
