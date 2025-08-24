package cn.sparrowmini.common.listener;

import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
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
public class InsertEventListener implements PreInsertEventListener {

	@Autowired
	private ModelPermissionService modelPermissionService;

	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		String modelId = event.getEntity().getClass().getName();

		if (modelId.equals(UserModel.class.getName()) || modelId.equals(SysroleModel.class.getName())
				|| modelId.equals(Model.class.getName())) {
			return false;
		}
		if(CurrentUser.get()!=null) {
			modelPermissionService.hasPermission(modelId, PermissionEnum.AUTHOR, CurrentUser.get(),
					CurrentUser.getUserInfo().getRoles());
		}

		return false;
	}

}
