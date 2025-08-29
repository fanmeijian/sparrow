package cn.sparrowmini.permission.sysrole.repository;

import cn.sparrowmini.common.repository.BaseStateRepository;
import cn.sparrowmini.permission.sysrole.model.Sysrole;

public interface SysroleRepository extends BaseStateRepository<Sysrole, String> {

    Sysrole findByCode(String name);
}
