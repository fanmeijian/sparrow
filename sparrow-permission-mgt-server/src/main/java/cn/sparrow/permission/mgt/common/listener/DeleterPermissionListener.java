package cn.sparrow.permission.mgt.common.listener;

import javax.persistence.PreRemove;
import javax.validation.ValidationException;

import cn.sparrow.permission.constant.PermissionEnum;
import cn.sparrow.permission.model.common.AbstractSparrowEntity;

public final class DeleterPermissionListener extends AbstractPermissionListener {

	@PreRemove
	private void beforeAnyUpdate(AbstractSparrowEntity abstractEntity) {
		this.init();
		String username = CurrentUser.INSTANCE.get();
//
//		if (!permissionService.hasPermission(employeeTokenService.getEmployeeTokenByUsername(username),
//				permissionTokenService.getModelPermissionToken(abstractEntity.getClass().getName()),
//				PermissionEnum.DELETER)) {
//			throw new ValidationException(
//					"SPR_MD_C_DN-40" + "模型拒绝删除权限" + abstractEntity.getClass().getName() + username);
//		}

	}
}
