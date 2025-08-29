package cn.sparrowmini.permission.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.model.ErrMessage;
import cn.sparrowmini.permission.model.relation.GroupUser;
import cn.sparrowmini.permission.repository.GroupUserRepository;
import cn.sparrowmini.permission.repository.UserRepository;
import cn.sparrowmini.permission.sysrole.model.Sysrole;
import cn.sparrowmini.permission.sysrole.model.UserSysrole;
import cn.sparrowmini.permission.sysrole.repository.SysroleRepository;
import cn.sparrowmini.permission.sysrole.repository.UserSysroleRepository;
import cn.sparrowmini.permission.user.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractUserServiceImpl implements UserService {

	private final UserSysroleRepository userSysroleRepository;
	private final SysroleRepository sysroleRepository;
	private final UserRepository userRepository;
	private final GroupUserRepository groupUserRepository;

	@Override
	public List<Sysrole> getSysroles(String username) {
		return this.userSysroleRepository.findByIdUsername(username).stream().map(f->sysroleRepository.findById(f.getId().getSysroleId()).orElse(null)).collect(Collectors.toList());
	}

	@Override
	public List<UserSysrole> getUserSysroles(String username) {
		return this.userSysroleRepository.findByIdUsername(username);
	}

	public List<GroupUser> groups(String username) {
		return this.groupUserRepository.findByIdUsername(username);
	}

	@Override
	public Page<User> getAllUsers(Pageable pageable, String filter) {
		return this.userRepository.findAll(pageable);
	}

	@Transactional
	@Override
	public Map<String, List<ErrMessage>> create(Set<User> users) {
		this.userRepository.saveAll(users);
		Map<String, List<ErrMessage>> map = new HashMap<>();
		users.forEach(u -> map.put(u.getUsername(), u.getErrMessages()));
		return map;
	}

	@Override
	public List<ErrMessage> update(@PathVariable String username, @RequestBody Map<String, Object> map) {
		User userRef = this.userRepository.getReferenceById(username);
//		PatchUpdateHelper.merge(userRef, map);
//		this.userRepository.save(userRef);
		return userRef.getErrMessages();
	}

	@Override
	public User get(String username) {
		return this.userRepository.findById(username).get();
	}

	@Transactional
	@Override
	public void 删除用户(Set<String> usernames) {
		this.userRepository.deleteAllById(usernames);
	}


	@Override
	public void enable(String username, Boolean enabled) {
		User userRef = this.userRepository.getReferenceById(username);
		userRef.setEnabled(enabled);
		this.userRepository.save(userRef);
	}

	@Override
	public void resetPassword(@PathVariable String username, @RequestBody String password){

	}

	@Override
	public List<Sysrole> mySysroles(){
		String username = CurrentUser.get();
		return userSysroleRepository.findByIdUsername(username).stream().map(UserSysrole::getSysrole).collect(Collectors.toList());
	}

}
