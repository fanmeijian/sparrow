package cn.sparrowmini.web;

import cn.sparrowmini.security.api.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/session")
public class SessionController {
    private final CurrentActorProvider actors;private final FunctionPermissionEvaluator permissions;private final PermissionCatalog registered;
    public SessionController(CurrentActorProvider actors,FunctionPermissionEvaluator permissions,PermissionCatalog registered){this.actors=actors;this.permissions=permissions;this.registered=registered;}
    @GetMapping public Map<String,Object> session(HttpServletRequest request){
        var actor=actors.current();var token=(CsrfToken)request.getAttribute(CsrfToken.class.getName());
        var allowed=registered.permissions().stream().filter(p->permissions.evaluate(actor,p).allowed()).sorted(Comparator.comparing(PermissionRequest::resource).thenComparing(PermissionRequest::scope)).toList();
        return Map.of("subject",actor.subject(),"permissions",allowed,"csrf",token==null?Map.of():Map.of("header",token.getHeaderName(),"token",token.getToken()));
    }
}
