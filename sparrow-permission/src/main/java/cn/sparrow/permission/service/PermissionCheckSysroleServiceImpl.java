package cn.sparrow.permission.service;

import org.springframework.stereotype.Service;
import cn.sparrow.model.common.PermissionEnum;
import cn.sparrow.model.permission.PermissionExpression;
import cn.sparrow.model.permission.Sysrole;

@Service
public class PermissionCheckSysroleServiceImpl implements PermissionCheckService<Sysrole, String> {

  @Override
  public boolean checkPermission(String id, PermissionExpression<Sysrole, String> permissionExpression,
      PermissionEnum permission) {
    // TODO Auto-generated method stub
    return false;
  }


}
