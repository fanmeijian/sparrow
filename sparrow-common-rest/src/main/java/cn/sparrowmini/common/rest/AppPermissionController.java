package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.service.MenuService;
import cn.sparrowmini.common.service.ModelService;
import cn.sparrowmini.common.service.PageElementService;
import cn.sparrowmini.common.service.ScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("permissions")
public class AppPermissionController {
    @Autowired
    private ScopeService scopeService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private PageElementService pageElementService;

    @Autowired
    private MenuService menuService;

    @PostMapping("/scopes/synchronize")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void synchronizeScope(){
        scopeService.synchronize();
    }

    @PostMapping("/models/synchronize")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void synchronizeModel(){
        modelService.synchronize();
    }

    @GetMapping("/page-elements/permissions")
    @ResponseBody
    public Map<String, PermissionTypeEnum> getPageElementPermission(@RequestParam("id") Set<String> ids){

        return pageElementService.pageElementByPage(ids, CurrentUser.getUserInfo());
    }

}
