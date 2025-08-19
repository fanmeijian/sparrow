package cn.sparrowmini.common.service;

import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.exception.DenyPermissionException;
import cn.sparrowmini.common.exception.NoPermissionException;
import cn.sparrowmini.common.model.ModelAttributeId;
import cn.sparrowmini.common.model.pem.SysroleModelAttribute;
import cn.sparrowmini.common.repository.SysroleModelAttributeRepository;
import cn.sparrowmini.common.repository.SysroleModelRepository;
import cn.sparrowmini.common.repository.UserModelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.sparrowmini.common.model.pem.UserModel;

import java.util.Collection;

@Slf4j
@Service
public class ModelPermissionService {
    @Autowired
    private SysroleModelRepository sysroleModelRepository;
    @Autowired
    private SysroleModelAttributeRepository sysroleModelAttributeRepository;
    @Autowired
    private UserModelRepository userModelRepository;
//
//    @Autowired
//    private ModelAttributeRuleRepository modelAttributeRuleRepository;


    /**
     * 检查模型权限
     *
     * @param modelId
     * @param permission
     * @param username
     * @return
     */
    public boolean hasPermission(String modelId, PermissionEnum permission, String username, Collection<String> roles) {

        if (PermissionUserAndRole.isSuperSysAdmin(username,roles)) {
            return true;
        }

        //个人名下的角色的授权
        boolean isDeny = this.sysroleModelRepository.isPermission(modelId, PermissionTypeEnum.DENY, username)
                || this.userModelRepository.existsById(new UserModel.UserModelId(modelId, username, PermissionTypeEnum.DENY, permission));
        if (isDeny) {
            throw new DenyPermissionException(
                    String.join(" ", "拒绝权限", modelId, permission.name(), username));
        }

        boolean isConfig = this.sysroleModelRepository.isConfig(modelId, PermissionTypeEnum.ALLOW)
                || this.userModelRepository.isConfig(modelId, PermissionTypeEnum.ALLOW);

        boolean isAllow = this.sysroleModelRepository.isPermission(modelId, PermissionTypeEnum.ALLOW, username)
                || this.userModelRepository.existsById(new UserModel.UserModelId(modelId, username, PermissionTypeEnum.ALLOW, permission));
        if (isConfig && !isAllow) {
            throw new NoPermissionException(String.join(" ", "没有权限", modelId, permission.name(), username));
        }

        return true;
    }

    public boolean hasPermission(ModelAttributeId attributePK, PermissionEnum permission, String username, Collection<String> roles) {
        if (PermissionUserAndRole.isSuperSysAdmin(username,roles)) {
            return true;
        }
        boolean allowPermissions = false;

        for (String sysrole : roles) {
            // check deny permission
            log.debug("sysrole: {}", sysrole);
            SysroleModelAttribute denyPermission = this.sysroleModelAttributeRepository
                    .findById(new SysroleModelAttribute.SysroleModelAttributeId(attributePK, sysrole,
                            PermissionTypeEnum.DENY, permission))
                    .orElse(null);

            if (denyPermission != null) {
                throw new DenyPermissionException(String.join(" ", "拒绝权限", attributePK.getModelId(),
                        attributePK.getAttributeId(), permission.name(), sysrole));
            }
            ;

            // check allow permission
            if (this.sysroleModelAttributeRepository.countByIdAttributeIdAndIdPermissionAndIdPermissionType(attributePK,
                    permission, PermissionTypeEnum.ALLOW) > 0) {
                SysroleModelAttribute allowPermission = this.sysroleModelAttributeRepository
                        .findById(new SysroleModelAttribute.SysroleModelAttributeId(attributePK, sysrole,
                                PermissionTypeEnum.ALLOW, permission))
                        .orElse(null);
                if (allowPermission != null) {
                    allowPermissions = true;
                }
                ;
            }
        }

        if (!allowPermissions) {
            throw new NoPermissionException(
                    String.join(" ", "没有权限", attributePK.getAttributeId(), permission.name(), username));
        }
        return true;
    }


    /**
     * 检查模型权限,使用规则表达式
     *
     * @param modelId
     * @param permission
     * @param username
     * @param entity
     * @return
     */
    public boolean hasPermission(String modelId, PermissionEnum permission, String username,Collection<String> roles, Object entity) {

        if (PermissionUserAndRole.isSuperSysAdmin(username,roles)) {
            return true;
        }

        //个人名下的角色的授权
        boolean isDeny = this.sysroleModelRepository.isPermission(modelId, PermissionTypeEnum.DENY, username)
                || this.userModelRepository.existsById(new UserModel.UserModelId(modelId, username, PermissionTypeEnum.DENY, permission));
        if (isDeny) {
            throw new DenyPermissionException(
                    String.join(" ", "拒绝权限", modelId, permission.name(), username));
        }

        boolean isConfig = this.sysroleModelRepository.isConfig(modelId, PermissionTypeEnum.ALLOW)
                || this.userModelRepository.isConfig(modelId, PermissionTypeEnum.ALLOW);

        boolean isAllow = this.sysroleModelRepository.isPermission(modelId, PermissionTypeEnum.ALLOW, username)
                || this.userModelRepository.existsById(new UserModel.UserModelId(modelId, username, PermissionTypeEnum.ALLOW, permission));
//        if (isConfig && !isAllow) {
//            throw new NoPermissionException(String.join(" ", "没有权限", modelId, permission.name(), username));
//        }

        boolean allowPermissions = false;

        if (isConfig && !isAllow) {
            return false;
        }

        return true;

    }

    /**
     * 检查模型属性权限,使用规则表达式
     *
     * @param attributePK
     * @param permission
     * @param username
     * @param entity
     * @return
     */
    public boolean hasPermission(ModelAttributeId attributePK, PermissionEnum permission, String username,Collection<String> roles,
                                 Object entity) {
        if (PermissionUserAndRole.isSuperSysAdmin(username,roles)) {
            return true;
        }

        boolean allowPermissions = false;
        int allowPermissionCount = 0;

        for (String sysrole : roles) {
            // check deny permission
            log.debug("sysrole: {}", sysrole);
            SysroleModelAttribute denyPermission = this.sysroleModelAttributeRepository
                    .findById(new SysroleModelAttribute.SysroleModelAttributeId(attributePK, sysrole,
                            PermissionTypeEnum.DENY, permission))
                    .orElse(null);

            if (denyPermission != null) {
                throw new DenyPermissionException(String.join(" ", "拒绝权限", attributePK.getModelId(),
                        attributePK.getAttributeId(), permission.name(), sysrole));
            }
            ;

            // check allow permission
            int allowCount = this.sysroleModelAttributeRepository
                    .countByIdAttributeIdAndIdPermissionAndIdPermissionType(attributePK, permission,
                            PermissionTypeEnum.ALLOW);
            allowPermissionCount = allowPermissionCount + allowCount;
            if (allowCount > 0) {
                SysroleModelAttribute allowPermission = this.sysroleModelAttributeRepository
                        .findById(new SysroleModelAttribute.SysroleModelAttributeId(attributePK, sysrole,
                                PermissionTypeEnum.ALLOW, permission))
                        .orElse(null);
                if (allowPermission != null) {
                    allowPermissions = true;
                }
                ;
            }
        }


        if (!allowPermissions && allowPermissionCount > 0) {
            throw new NoPermissionException(
                    String.join(" ", "没有权限", attributePK.getAttributeId(), permission.name(), username));
        }
        return true;
    }

}
