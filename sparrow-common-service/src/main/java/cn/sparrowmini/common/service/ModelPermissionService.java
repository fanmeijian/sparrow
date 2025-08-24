package cn.sparrowmini.common.service;

import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.exception.DenyPermissionException;
import cn.sparrowmini.common.exception.NoPermissionException;
import cn.sparrowmini.common.model.pem.SysroleModel;
import cn.sparrowmini.common.model.pem.UserModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class ModelPermissionService {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    /**
     * 检查模型权限
     *
     * @param modelId
     * @param permission
     * @param username
     * @return
     */
    public boolean hasPermission(String modelId, PermissionEnum permission, String username, Collection<String> roles) {

        if (PermissionUserAndRole.isSuperSysAdmin(username, roles)) {
            return true;
        }

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            // 用 em 查询权限，不影响原实体
            boolean isUserDeny = em.createNamedQuery("UserModel.existByPermission", Boolean.class)
                    .setParameter("modelId", modelId)
                    .setParameter("permission", permission)
                    .setParameter("permissionType", PermissionTypeEnum.DENY)
                    .getSingleResult();

            if (isUserDeny) {
                throw new DenyPermissionException(
                        String.join(" ", "用户拒绝权限", modelId, permission.name(), username));
            }

            boolean isRoleDeny = em.createNamedQuery("SysroleModel.existByPermission", Boolean.class)
                    .setParameter("modelId", modelId)
                    .setParameter("permission", permission)
                    .setParameter("permissionType", PermissionTypeEnum.DENY)
                    .getSingleResult();

            if (isRoleDeny) {
                throw new DenyPermissionException(
                        String.join(" ", "角色拒绝权限", modelId, permission.name(), username));
            }


            /**
             * 是否配置了用户的权限
             */
            boolean isUserAllowConfig = em.createNamedQuery("UserModel.existByPermission", Boolean.class)
                    .setParameter("modelId", modelId)
                    .setParameter("permission", permission)
                    .setParameter("permissionType", PermissionTypeEnum.DENY)
                    .getSingleResult();

            /**
             * 是否配置了角色的权限
             */
            boolean isRoleAllowConfig = em.createNamedQuery("SysroleModel.existByPermission", Boolean.class)
                    .setParameter("modelId", modelId)
                    .setParameter("permission", permission)
                    .setParameter("permissionType", PermissionTypeEnum.DENY)
                    .getSingleResult();

            boolean isAllowConfig = isRoleAllowConfig || isUserAllowConfig;

            /**
             * 自己是否有权限
             */

            if (isAllowConfig) {
                boolean isUserAllow = em.createNamedQuery("UserModel.existById", Boolean.class)
                        .setParameter("id", new UserModel.UserModelId(modelId, username, PermissionTypeEnum.ALLOW, permission))
                        .getSingleResult();

                List<SysroleModel.SysroleModelId> sysroleModelIdList = roles.stream().map(m -> new SysroleModel.SysroleModelId(modelId, m, PermissionTypeEnum.ALLOW, permission)).toList();
                boolean isRoleAllow = em.createNamedQuery("SysroleModel.existInIds", Boolean.class)
                        .setParameter("ids", sysroleModelIdList)
                        .getSingleResult();

                if (isRoleAllow || isUserAllow) {

                } else {
                    throw new NoPermissionException(
                            String.join(" ", "没有权限", modelId, permission.name(), username));
                }
            }

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return true;
    }
}
