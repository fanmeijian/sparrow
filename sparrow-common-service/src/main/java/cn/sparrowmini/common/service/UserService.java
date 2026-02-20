package cn.sparrowmini.common.service;

import cn.sparrowmini.common.model.ErrMessage;
import cn.sparrowmini.common.model.UserDto;
import cn.sparrowmini.common.model.pem.Sysrole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    public List<Sysrole> getSysroleList(String username);

    public Page<UserDto> getUserList(Pageable pageable, String filter);

    public void synchronize();

    public Map<String, List<ErrMessage>> create(Set<UserDto> users);

    public List<ErrMessage> update(String username, Map<String, Object> map);

    public UserDto get(String username);

    public void delete(Set<String> usernames);

    public void enable(String username, Boolean enabled);

    public void resetPassword(String username, String password);


}
