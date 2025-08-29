package cn.sparrowmini.permission.service;


import cn.sparrowmini.common.constant.MenuTypeEnum;
import cn.sparrowmini.common.constant.SysPermissionTarget;
import cn.sparrowmini.common.model.Menu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "menu", description = "菜单服务")
@RequestMapping("/menus")
public interface MenuService {

    @GetMapping("/my")
    @Operation(summary = "我的菜单", operationId = "myMenu")
    @ResponseBody
    Page<Menu> getMyTree(@Nullable String parentId, String appId, @Nullable MenuTypeEnum type);

    @GetMapping("/tree")
    @Operation(summary = "我的菜单")
    @ResponseBody
    Page<Menu> getMenuTree(@Nullable String parentId);

}
