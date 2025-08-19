package cn.sparrowmini.common.service;

import cn.sparrowmini.common.dto.MenuPermissionBean;
import cn.sparrowmini.common.model.Menu;
import cn.sparrowmini.common.model.pem.UserMenu;
import cn.sparrowmini.common.model.pem.SysroleMenu;
import cn.sparrowmini.common.repository.MenuRepository;
import cn.sparrowmini.common.service.scopeconstant.MenuScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private CommonJpaService commonJpaService;

    public Page<Menu> getTreeByUsername(String parentId, String username, Set<String> roles, Pageable pageable) {
//        Sort sort = Sort.by("seq").ascending().and(Sort.by("createdDate").ascending());
//        Pageable pageable = PageRequest.of(0, 2000, sort);
        pageable.getSort().and(Sort.by("seq").ascending());
        if(parentId!=null && menuRepository.existsByCode(parentId)){
            parentId = menuRepository.getReferenceByCode(parentId).getId();
        }

        Specification<Menu> spec = Specification
                .where(MenuSpecifications.hasParentIdOrNull(parentId))
                .and(MenuSpecifications.userHasAccessToMenu(username, roles))
                ;

        Page<Menu> result = menuRepository.findAll(spec, pageable);
        result.forEach(menu -> {
            long count = menuRepository.countByParentId(menu.getId());
            menu.setChildCount(count);
        });
        return result;
    }

    @Transactional
    @ScopePermission(name = "增加菜单权限", scope = MenuScope.MenuPemScope.ADD)
    public void addPermission(MenuPermissionBean menuPermissionBean) {
        if (menuPermissionBean.getUserMenuIds() != null) {
            this.commonJpaService.saveEntity(UserMenu.class,menuPermissionBean.getUserMenuIds().stream().map(UserMenu::new).collect(Collectors.toList()));
        }

        if (menuPermissionBean.getSysroleMenuIds() != null) {
            this.commonJpaService.saveEntity(SysroleMenu.class,menuPermissionBean.getSysroleMenuIds().stream().map(SysroleMenu::new).collect(Collectors.toList()));
        }
    }

    @Transactional
    @ScopePermission(name = "删除菜单权限", scope = MenuScope.MenuPemScope.REMOVE)
    public void delPermission(MenuPermissionBean menuPermissionBean) {
        if (menuPermissionBean.getUserMenuIds() != null) {
            commonJpaService.deleteEntity(UserMenu.class,menuPermissionBean.getUserMenuIds());
        }

        if (menuPermissionBean.getSysroleMenuIds() != null) {
            commonJpaService.deleteEntity(SysroleMenu.class,menuPermissionBean.getSysroleMenuIds());
        }
    }
}
