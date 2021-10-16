package cn.sparrow.model.group;

import java.io.Serializable;
import javax.persistence.Embeddable;

import cn.sparrow.model.permission.AbstractDataFieldPermissionPK;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class GroupDataFieldPermissionPK extends AbstractDataFieldPermissionPK implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String groupId;
}