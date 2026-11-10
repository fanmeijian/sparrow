package cn.sparrowmini.security.spring;

import cn.sparrowmini.security.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PermissionAspectTest {
    public static class Operations {
        int calls;
        @RequiresPermission(resource="invoice",scope="update")
        public String update(){calls++;return "updated";}
    }
    @Test void deniedCallNeverReachesBusinessCode(){
        var target=new Operations();var actor=new ActorContext("user",Set.of(),Set.of());
        var factory=new AspectJProxyFactory(target);
        factory.addAspect(new PermissionAspect(()->actor,(a,p)->new FunctionDecision(a,p,false,Set.of())));
        Operations proxy=factory.getProxy();assertThrows(SecurityException.class,proxy::update);assertEquals(0,target.calls);
    }
    @Test void allowedCallUsesCurrentActorAndExactScope(){
        var target=new Operations();var actor=new ActorContext("user",Set.of("editor"),Set.of());
        var factory=new AspectJProxyFactory(target);
        factory.addAspect(new PermissionAspect(()->actor,(a,p)->{
            assertSame(actor,a);assertEquals(new PermissionRequest("invoice","update"),p);
            return new FunctionDecision(a,p,true,Set.of("editor"));
        }));
        Operations proxy=factory.getProxy();assertEquals("updated",proxy.update());assertEquals(1,target.calls);
    }
}
