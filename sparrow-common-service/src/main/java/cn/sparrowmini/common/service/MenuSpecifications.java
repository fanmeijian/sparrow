package cn.sparrowmini.common.service;

import cn.sparrowmini.common.model.Menu;
import cn.sparrowmini.common.model.pem.SysroleMenu;
import cn.sparrowmini.common.model.pem.UserMenu;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public class MenuSpecifications {

    public static Specification<Menu> userHasAccessToMenu(String username, Set<String> roles) {
        return (root, query, cb) -> {

            if(PermissionUserAndRole.isSuperSysAdmin(username, roles)){
                return cb.conjunction();
            }

            // EXISTS (SELECT 1 FROM UserMenu um WHERE um.menuId = m.id AND um.username = :username)
            Subquery<Long> userMenuSub = query.subquery(Long.class);
            Root<UserMenu> um = userMenuSub.from(UserMenu.class);
            userMenuSub.select(cb.literal(1L))
                    .where(
                            cb.equal(um.get("id").get("menuId"), root.get("id")),
                            cb.equal(um.get("id").get("username"), username)
                    );

            // EXISTS (SELECT 1 FROM SysroleMenu sm WHERE sm.menuId = m.id AND sm.sysroleId IN (
            //     SELECT us.sysroleId FROM UserSysrole us WHERE us.username = :username
            // ))
            Subquery<Long> roleMenuSub = query.subquery(Long.class);
            Root<SysroleMenu> sm = roleMenuSub.from(SysroleMenu.class);

            roleMenuSub.select(cb.literal(1L))
                    .where(
                            cb.equal(sm.get("id").get("menuId"), root.get("id")),
                            sm.get("id").get("sysroleId").in(roles)
                    );

            return cb.or(
                    cb.exists(userMenuSub),
                    cb.exists(roleMenuSub)
            );

        };


    }

    public static Specification<Menu> hasAppId(String appId) {
        return (root, query, cb) -> cb.equal(root.get("appId"), appId);
    }

    public static Specification<Menu> hasParentIdOrNull(String parentId) {
        return (root, query, cb) -> {
            if (parentId == null) {
                return cb.isNull(root.get("parentId"));
            } else {
                return cb.equal(root.get("parentId"), parentId);
            }
        };
    }
}