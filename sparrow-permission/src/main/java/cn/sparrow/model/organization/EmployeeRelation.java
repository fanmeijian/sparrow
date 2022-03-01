package cn.sparrow.model.organization;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;

import cn.sparrow.model.common.AbstractOperationLog;
import cn.sparrow.permission.listener.RepositoryErrorFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "spr_employee_relation")
@EntityListeners(AuditingEntityListener.class)
public class EmployeeRelation extends AbstractOperationLog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@EqualsAndHashCode.Include
	@EmbeddedId
	private EmployeeRelationPK id;

	@ManyToOne
	@JoinColumn(name = "employee_id", insertable = false, updatable = false)
	private Employee employee;

	@ManyToOne
	@JoinColumn(name = "parent_id	", insertable = false, updatable = false)
	private Employee parent;

	public EmployeeRelation(EmployeeRelationPK f) {
		this.id = f;
	}

	@PrePersist
	@PreUpdate
	private void preSave() {
		if (id.getEmployeeId().equals(id.getParentId())) {
			throw new RepositoryConstraintViolationException(
					RepositoryErrorFactory.getErros(this, "", "can not add relation to self"));
		}
	}
}
