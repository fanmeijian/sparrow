package cn.sparrowmini.user.service;

import cn.sparrowmini.common.model.ErrMessage;
import cn.sparrowmini.user.model.Sysrole;
import cn.sparrowmini.user.model.UserSysrole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    public List<Sysrole> getSysroles(String username);

    public List<UserSysrole> getUserSysroles(String username);


    public Page<User> getAllUsers(Pageable pageable, String filter);


    public void synchronize();


    public Map<String, List<ErrMessage>> create(Set<User> users);


    public List<ErrMessage> update(String username, Map<String, Object> map);


    public User get(String username);

    public void 删除用户(Set<String> usernames);


    public UserToken token(String username);


    public void enable(String username, Boolean enabled);


    public void resetPassword(String username, String password);

    public List<Sysrole> mySysroles();

}
