package cn.sparrow.permission.service;

import org.springframework.stereotype.Service;
import cn.sparrow.model.common.PermissionEnum;
import cn.sparrow.model.organization.Employee;
import cn.sparrow.model.permission.token.PermissionExpression;

@Service
public class EmployeePermissionCheckService implements PermissionCheckService<Employee, String> {

  @Override
  public boolean checkPermission(String id, PermissionExpression<?, ?> permissionExpression,
      PermissionEnum permission) {
    // TODO Auto-generated method stub
    return false;
  }


}
