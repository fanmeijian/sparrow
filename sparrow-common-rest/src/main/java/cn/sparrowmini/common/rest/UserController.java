package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.model.Menu;
import cn.sparrowmini.common.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private MenuService menuService;

    @GetMapping("menus")
    @ResponseBody
    public Page<Menu> getMyMenus(String parentId) {
        String username = CurrentUser.getUserInfo().getUsername();
        Set<String> roles = CurrentUser.getUserInfo().getRoles();
        return menuService.getTreeByUsername(parentId,username,roles, PageRequest.of(0, Integer.MAX_VALUE));
    }

}
