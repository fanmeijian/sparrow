package cn.sparrow.model.group;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import cn.sparrow.model.common.AbstractOperationLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity implementation class for Entity: GroupModelPermission
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "spr_group_model_permission")

public class GroupModelPermission extends AbstractOperationLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private GroupModelPermissionPK id;

}