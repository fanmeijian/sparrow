package cn.sparrowmini.common.model;

import cn.sparrowmini.common.model.pem.group.Group;
import cn.sparrowmini.common.model.pem.Sysrole;
import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private String username;
    private String name;
    private List<Group> groups;
    private List<Sysrole> roles;
}
