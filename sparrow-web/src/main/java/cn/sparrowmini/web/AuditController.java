package cn.sparrowmini.web;

import cn.sparrowmini.system.audit.AuditService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin/audit")
public class AuditController {
    private final AuditService audit;
    public AuditController(AuditService audit){this.audit=audit;}
    @GetMapping public List<AuditService.Event> list(@RequestParam(defaultValue="0")int offset,@RequestParam(defaultValue="50")int limit){return audit.list(offset,limit);}
}
