package cn.sparrowmini.common.dto;

import cn.sparrowmini.common.model.pem.SysroleMenu;
import cn.sparrowmini.common.model.pem.UserMenu;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MenuPermissionBean implements Serializable {
    private List<UserMenu.UserMenuId> userMenuIds;
    private List<SysroleMenu.SysroleMenuId> sysroleMenuIds;

}
