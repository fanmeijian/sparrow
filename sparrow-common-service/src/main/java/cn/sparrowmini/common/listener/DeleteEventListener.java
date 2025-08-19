package cn.sparrowmini.common.listener;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.service.ModelPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeleteEventListener implements PreDeleteEventListener {


	@Autowired
	private ModelPermissionService modelPermissionService;

	@Override
	public boolean onPreDelete(PreDeleteEvent event) {
		String modelId = event.getEntity().getClass().getName();

		modelPermissionService.hasPermission(modelId, PermissionEnum.DELETER,CurrentUser.get(),CurrentUser.getUserInfo().getRoles());

//		if (event.getEntity().getClass().isAnnotationPresent(ModelPermission.class)) {
//			return !this.dataPermissionService.hasPermission(event.getEntityName(), PermissionEnum.DELETER,
//					CurrentUser.get(), event.getEntity());
//
//			//检查数据权限
//		} else {
//			return false;
//		}
		return false;
	}

}
