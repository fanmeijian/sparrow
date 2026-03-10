package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.model.pem.Sysrole;
import cn.sparrowmini.common.service.SysroleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sysroles")
public class SysroleController {
    @Autowired
    private SysroleService sysroleService;

    @GetMapping
    public Page<Sysrole> getSysroleList(Pageable pageable, String filter) {
        return sysroleService.all(pageable,filter);
    }
}
