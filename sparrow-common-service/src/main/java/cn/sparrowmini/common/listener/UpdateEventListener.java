package cn.sparrowmini.common.listener;

import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.model.Model;
import cn.sparrowmini.common.model.pem.SysroleModel;
import cn.sparrowmini.common.model.pem.UserModel;
import cn.sparrowmini.common.service.ModelPermissionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdateEventListener implements PreUpdateEventListener {

	@Autowired
	private ModelPermissionService modelPermissionService;


	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {
		String modelId = event.getEntity().getClass().getName();

		if (modelId.equals(UserModel.class.getName()) || modelId.equals(SysroleModel.class.getName())
				|| modelId.equals(Model.class.getName())) {
			return false;
		}

		if(CurrentUser.get()!=null) {
			modelPermissionService.hasPermission(modelId, PermissionEnum.EDITOR, CurrentUser.get(),
					CurrentUser.getUserInfo().getRoles());
		}

		return false;
	}

}
