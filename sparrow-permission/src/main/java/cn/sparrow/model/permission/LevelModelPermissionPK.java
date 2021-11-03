package cn.sparrow.model.permission;

import java.io.Serializable;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class LevelModelPermissionPK extends AbstractModelPermissionPK implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String positionLevelId;

}