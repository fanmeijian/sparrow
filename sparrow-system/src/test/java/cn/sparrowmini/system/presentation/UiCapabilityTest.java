package cn.sparrowmini.system.presentation;
import cn.sparrowmini.security.api.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static cn.sparrowmini.system.presentation.UiCapabilityService.*;
import static org.junit.jupiter.api.Assertions.*;
class UiCapabilityTest {
    final PermissionRequest read=new PermissionRequest("order","read"),edit=new PermissionRequest("order","update");
    final ActorContext actor=new ActorContext("alice",Set.of("sales"),Set.of());
    Element button(String parent,Mode mode,boolean hide){return new Element("edit",parent,Kind.BUTTON,false,read,edit,null,mode,hide);}
    @Test void rowContextControlsEachButtonAndDenialCanHide(){
        var service=new UiCapabilityService(List.of(button(null,Mode.DERIVED,false)),List.of(),(a,p,id,f,o)->p.equals(read)||"own".equals(id));
        assertTrue(service.decide(actor,"edit","own").enabled());assertFalse(service.decide(actor,"edit","other").enabled());assertTrue(service.decide(actor,"edit","other").visible());
        var hidden=new UiCapabilityService(List.of(button(null,Mode.DERIVED,true)),List.of(),(a,p,id,f,o)->p.equals(read));assertFalse(hidden.decide(actor,"edit","other").visible());
    }
    @Test void explicitUiAllowCannotOverrideBusinessDenial(){
        var subject=new Subject(Subject.Kind.USER,"alice");
        var policies=List.of(new Policy("edit",subject,Operation.VIEW,Effect.ALLOW),new Policy("edit",subject,Operation.INTERACT,Effect.ALLOW));
        var service=new UiCapabilityService(List.of(button(null,Mode.RESTRICTED,false)),policies,(a,p,id,f,o)->p.equals(read));
        assertTrue(service.decide(actor,"edit","1").visible());assertFalse(service.decide(actor,"edit","1").enabled());
    }
    @Test void restrictedModeWithoutGrantIsHidden(){
        var service=new UiCapabilityService(List.of(button(null,Mode.RESTRICTED,false)),List.of(),(a,p,id,f,o)->true);assertFalse(service.decide(actor,"edit","1").visible());
    }
    @Test void fieldReadOnlyAndHiddenHaveDifferentMeaning(){
        var field=new Element("cost",null,Kind.FIELD,false,read,edit,"cost",Mode.DERIVED,false);
        var service=new UiCapabilityService(List.of(field),List.of(),(a,p,id,f,o)->p.equals(read)&&!"secret".equals(id));
        assertTrue(service.decide(actor,"cost","own").readOnly());assertFalse(service.decide(actor,"cost","secret").visible());
    }
    @Test void hiddenParentAndDenyRuleCannotBeReenabledByChild(){
        var panel=new Element("panel",null,Kind.PANEL,false,read,null,null,Mode.RESTRICTED,false);
        var service=new UiCapabilityService(List.of(panel,button("panel",Mode.DERIVED,false)),List.of(),(a,p,id,f,o)->true);
        assertFalse(service.decide(actor,"edit","1").visible());
        var subject=new Subject(Subject.Kind.ROLE,"sales");
        var denial=new UiCapabilityService(List.of(button(null,Mode.RESTRICTED,false)),List.of(new Policy("edit",subject,Operation.VIEW,Effect.ALLOW),new Policy("edit",subject,Operation.VIEW,Effect.DENY)),(a,p,id,f,o)->true);
        assertFalse(denial.decide(actor,"edit","1").visible());
    }
    @Test void malformedRegistryFailsAndResolverErrorsDoNotEnable(){
        assertThrows(IllegalArgumentException.class,()->new UiCapabilityService(List.of(button("missing",Mode.DERIVED,false)),List.of(),(a,p,id,f,o)->true));
        var service=new UiCapabilityService(List.of(button(null,Mode.DERIVED,false)),List.of(),(a,p,id,f,o)->{throw new IllegalStateException("Unavailable");});
        assertThrows(IllegalStateException.class,()->service.decide(actor,"edit","1"));
    }
}
