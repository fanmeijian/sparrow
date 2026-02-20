package cn.sparrowmini.common.service;


import cn.sparrowmini.common.SparrowTree;
import cn.sparrowmini.common.model.pem.dto.GroupEntityDto;
import cn.sparrowmini.common.model.pem.group.*;
import cn.sparrowmini.common.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupEntityRepository<GroupUser> groupUserRepository;

    @Autowired
    private GroupEntityRepository<GroupSysrole> groupSysroleRepository;

    @Autowired
    private GroupEntityRepository<GroupRelation> groupRelationRepository;

    @Autowired
    private GroupEntityRepository<GroupEntity> baseGroupEntityRepository;

    public Page<Group> list(Pageable pageable, String filter){
        return groupRepository.findAll(pageable, filter);
    }

    @Transactional
    public List<String> create(List<Map<String, Object>> groups){
        return groupRepository.upsert(groups);
    }

    @Transactional
    public void delete(Collection<String> groupIds){
        groupRepository.deleteAllById(groupIds);
    }

    public Optional<Group> get(String groupId){
        return groupRepository.findById(groupId);
    }

    @Transactional
    public void addEntity(Map<String, GroupEntityDto> groupEntityDtoMap){
        List<GroupUser> groupUsers = new ArrayList<>();
        List<GroupSysrole> groupSysroles = new ArrayList<>();
        List<GroupRelation> groupRelations = new ArrayList<>();
        for(String groupId : groupEntityDtoMap.keySet()){
            GroupEntityDto groupEntityDto = groupEntityDtoMap.get(groupId);
            if(groupEntityDto.getUsernames()!=null){
                groupUsers = groupEntityDto.getUsernames().stream().map(m->new GroupUser(groupId,m)).toList();
            }

            if(groupEntityDto.getSysroleIds()!=null){
                groupSysroles = groupEntityDto.getSysroleIds().stream().map(m->new GroupSysrole(groupId,m)).toList();
            }

            if(groupEntityDto.getSysroleIds()!=null){
                groupRelations = groupEntityDto.getGroupIds().stream().map(m->new GroupRelation(groupId,m)).toList();
            }
        }
        groupUserRepository.saveAll(groupUsers);
        groupSysroleRepository.saveAll(groupSysroles);
        groupRelationRepository.saveAll(groupRelations);

    }

    @Transactional
    public void removeEntity(String groupId, GroupEntityDto groupUser){
        List<GroupEntity.GroupEntityId> groupEntityIds = new ArrayList<>();
        if(groupUser.getGroupIds()!=null){
            groupEntityIds.addAll(groupUser.getGroupIds().stream().map(m->new GroupEntity.GroupEntityId(groupId, m)).toList());
        }

        if(groupUser.getSysroleIds()!=null){
            groupEntityIds.addAll(groupUser.getSysroleIds().stream().map(m->new GroupEntity.GroupEntityId(groupId, m)).toList());
        }

        if(groupUser.getUsernames()!=null){
            groupEntityIds.addAll(groupUser.getUsernames().stream().map(m->new GroupEntity.GroupEntityId(groupId, m)).toList());
        }

        baseGroupEntityRepository.deleteAllByIdInBatch(groupEntityIds);
    }

    public Page<?> groupEntityList(String groupId, GroupTypeEnum type, Pageable pageable){
//        switch (type){
//            case GROUP -> {
//                return groupRelationRepository.findBy(pageable);
//            }
//            case SYSROLE -> {
//                return groupSysroleRepository.findAll(pageable);
//            }
//            case USER ->  {
//                return groupUserRepository.findAll(pageable);
//            }
//        }
        return Page.empty();
    }

    public SparrowTree<Group, String> expandTree(String groupId){
        return null;
    }

    public List<?> expandFlat(String groupId, GroupTypeEnum type){
        return null;
    }

}
