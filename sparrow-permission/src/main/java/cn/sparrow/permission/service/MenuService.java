package cn.sparrow.permission.service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.sparrow.common.repository.UserMenuRepository;
import cn.sparrow.model.common.MyTree;
import cn.sparrow.model.permission.Menu;
import cn.sparrow.model.permission.MenuPermission;
import cn.sparrow.model.permission.SysroleMenu;
import cn.sparrow.model.permission.UserMenu;
import cn.sparrow.permission.repository.MenuRepository;
import cn.sparrow.permission.repository.SysroleMenuRepository;

@Service
public class MenuService {
	private static Logger logger = LoggerFactory.getLogger(MenuService.class);

	@Autowired
	MenuRepository menuRepository;

	@Autowired
	UserMenuRepository userMenuRepository;
	
	@Autowired
	SysroleMenuRepository sysroleMenuRepository;

	public MyTree<Menu> getTreeByParentId(String parentId) {
		Menu menu = menuRepository.findById(parentId).orElse(null);
		if (menu == null) {
			menu = new Menu();
			menu.setId(null);
			menu.setParentId(null);
		}
		MyTree<Menu> menuTree = new MyTree<Menu>(menu);
		buildTree(menuTree);
		return menuTree;
	}

	public void buildTree(MyTree<Menu> menuTree) {
		List<Menu> menus = menuRepository.findByParentId(menuTree.getMe().getId());
		for (Menu menu : menus) {
			MyTree<Menu> leaf = new MyTree<Menu>(menu);
			menuTree.getChildren().add(leaf);
			buildTree(leaf);
		}
	}

	public MyTree<Menu> getTreeByUsername(String username) {
		MyTree<Menu> menuTree = new MyTree<Menu>(new Menu());
		buildUserTree(username, menuTree);
		return menuTree;
	}

	public MyTree<Menu> getTreeBySysroleId(String sysroleId) {
		MyTree<Menu> menuTree = new MyTree<Menu>(new Menu());
		buildSysroleTree(sysroleId, menuTree);
		return menuTree;
	}

	// 构建角色菜单的大树，含直接父级的菜单
	public void buildSysroleTree(String sysroleId, MyTree<Menu> menuTree) {
		Set<Menu> menusSet = new HashSet<Menu>();
		getSysroleMenusWithParentAndChildren(sysroleId, menusSet);

		// 构建用户的菜单树
		List<Menu> menus = menuRepository.findByParentId(menuTree.getMe().getId());
		for (Menu menu : menus) {
			MyTree<Menu> leaf = new MyTree<Menu>(menu);
			if (menusSet.stream().anyMatch(p -> p.equals(menu)))
				menuTree.getChildren().add(leaf);
			buildTree(leaf);
		}
	}

	// 构建用户菜单的大树，含直接父级的菜单
	public void buildUserTree(String username, MyTree<Menu> menuTree) {
		Set<Menu> menusSet = new HashSet<Menu>();
		getUserMenusWithParentAndChildren(username, menusSet);

		// 构建用户的菜单树
		List<Menu> menus = menuRepository.findByParentId(menuTree.getMe().getId());
		for (Menu menu : menus) {
			MyTree<Menu> leaf = new MyTree<Menu>(menu);
			if (menusSet.stream().anyMatch(p -> p.equals(menu)))
				menuTree.getChildren().add(leaf);
			buildTree(leaf);
		}
	}

	public boolean isParent() {
		return false;
	}

	public boolean isChildren() {
		return false;
	}

	// 获取用户菜单的亲戚集合（不含兄弟姐妹节点）
	public void getUserMenusWithParentAndChildren(String username, Set<Menu> menus) {
		// menus.addAll(userRepository.findById(username).get().getMenus()) ;
		userMenuRepository.findByIdUsername(username).forEach(f -> {
			menus.add(f.getMenu());
			buildParents(f.getMenu().getParentId(), menus);
			buildChildren(f.getMenu().getId(), menus);
		});
	}

	// 获取角色菜单的亲戚集合（不含兄弟姐妹节点）
	public void getSysroleMenusWithParentAndChildren(String sysroleId, Set<Menu> menus) {
		// menus.addAll(userRepository.findById(username).get().getMenus()) ;
		sysroleMenuRepository.findByIdSysroleId(sysroleId).forEach(f -> {
			menus.add(f.getMenu());
			buildParents(f.getMenu().getParentId(), menus);
			buildChildren(f.getMenu().getId(), menus);
		});
	}

	// 获取到所有的祖先集合
	public void buildParents(String parentId, Set<Menu> menus) {

		if (parentId != null) {
			Menu parent = menuRepository.findById(parentId).orElse(null);
			menus.add(parent);
			buildParents(parent.getParentId(), menus);
		}
	}

	// 获取到所有的子孙集合
	public void buildChildren(String menuId, Set<Menu> menus) {
		menuRepository.findByParentId(menuId).forEach(f -> {
			menus.add(f);
			buildChildren(f.getId(), menus);
		});
	}
	
	public void addPermissions(MenuPermission menuPermission) {
		if(menuPermission.getUserMenuPKs()!=null) {
			menuPermission.getUserMenuPKs().forEach(f->{
				userMenuRepository.save(new UserMenu(f));
			});
		}
		
		if(menuPermission.getSysroleMenuPKs()!=null) {
			menuPermission.getSysroleMenuPKs().forEach(f->{
				sysroleMenuRepository.save(new SysroleMenu(f));
			});
		}
	}
	
	public void delPermissions(MenuPermission menuPermission) {
		if(menuPermission.getUserMenuPKs()!=null) {
			userMenuRepository.deleteByIdIn(menuPermission.getUserMenuPKs());
		}
		
		if(menuPermission.getSysroleMenuPKs()!=null) {
			sysroleMenuRepository.deleteByIdIn(menuPermission.getSysroleMenuPKs());
		}
	}

	public void setPosition(Menu menu) {
		LinkedList<Menu> linkedList = new LinkedList<Menu>();
		
		Menu m = menuRepository.getOne(menu.getId());
		
		if(menu.getPreviouNodeId()==null) {
			menuRepository.getOne
		}
		
	}

}
