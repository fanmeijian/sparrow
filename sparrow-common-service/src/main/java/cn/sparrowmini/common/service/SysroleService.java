package cn.sparrowmini.common.service;

import cn.sparrowmini.common.model.pem.Sysrole;
import cn.sparrowmini.common.model.pem.Sysrole_;
import cn.sparrowmini.common.model.pem.UserSysrole;
import cn.sparrowmini.common.model.pem.UserSysrole_;
import cn.sparrowmini.common.repository.SysroleRepository;
import cn.sparrowmini.common.repository.UserSysroleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SysroleService {

    @Autowired
    private SysroleRepository sysroleRepository;

    @Autowired
    private UserSysroleRepository userSysroleRepository;

    public Page<Sysrole> all(Pageable pageable, String filter){
        return sysroleRepository.findAll(pageable,filter);
    }

    @Transactional
    public List<String> create(List<Map<String, Object>> sysroles){
        return sysroleRepository.upsert(sysroles);
    }

    public Optional<Sysrole> get(String sysroleId){
        return sysroleRepository.findById(sysroleId);
    }

    @Transactional
    public void delete(List<String> ids){
        sysroleRepository.deleteAllById(ids);
    }

    @Transactional
    public void addUser(List<UserSysrole.UserSysroleId> userSysroleIds){
        userSysroleRepository.saveAll(userSysroleIds.stream().map(UserSysrole::new).toList());
    }

    @Transactional
    public void removeUser(List<UserSysrole.UserSysroleId> userSysroleIds){
        userSysroleRepository.deleteAllById(userSysroleIds);
    }

    public Page<UserSysrole> getUserList(String sysroleId, Pageable pageable){
        return userSysroleRepository.findBy(sysroleId,pageable,UserSysrole_.SYSROLE, Sysrole_.ID);
    }

}
