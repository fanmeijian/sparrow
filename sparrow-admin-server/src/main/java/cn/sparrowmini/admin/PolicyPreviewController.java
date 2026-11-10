package cn.sparrowmini.admin;

import cn.sparrowmini.core.query.Condition;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.identity.local.LocalIdentityProvider;
import cn.sparrowmini.rbac.persistence.jpa.JpaRoleProvider;
import cn.sparrowmini.group.persistence.jpa.JpaGroupService;
import cn.sparrowmini.datapolicy.core.*;
import cn.sparrowmini.datapolicy.persistence.jpa.JpaPolicyStore;
import cn.sparrowmini.file.*;
import cn.sparrowmini.file.policy.FilePolicyService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/admin/policies/preview")
public class PolicyPreviewController {
    private final EntityManager em;private final String namespace;private final AdminAccess access;private final LocalIdentityProvider users;
    private final JpaRoleProvider roles;private final JpaGroupService groups;private final JpaPolicyStore policies;private final FileReferenceService references;
    public PolicyPreviewController(EntityManager em,@Value("${sparrow.identity.namespace:default}")String namespace,AdminAccess access,LocalIdentityProvider users,JpaRoleProvider roles,JpaGroupService groups,JpaPolicyStore policies,FileReferenceService references){this.em=em;this.namespace=namespace;this.access=access;this.users=users;this.roles=roles;this.groups=groups;this.policies=policies;this.references=references;}
    public record Preview(String subject,String scope,String recordId) { }
    public record Field(boolean readable,boolean writable) { }
    public record Result(boolean functionAllowed,boolean recordAllowed,Set<String> eligibleRoles,Map<String,Field> fields) { }
    @PostMapping @Transactional(readOnly=true)
    public Result preview(@RequestBody Preview input){
        access.require("admin.policies","read");
        if(input.subject()==null||!input.subject().startsWith(namespace+":")||input.recordId()==null||input.scope()==null||!Set.of("read","update","delete","download").contains(input.scope()))throw new IllegalArgumentException("Invalid preview input");
        var user=users.get(input.subject().substring(namespace.length()+1));
        var actor=new ActorContext(input.subject(),Set.of(),groups.facts().graph().effectiveGroups(input.subject()));
        var request=new PermissionRequest("file.object",input.scope());var decision=roles.evaluate(actor,request);
        if(!user.enabled())return new Result(false,false,Set.of(),Map.of());
        var plan=new DataAccessPlan(decision,policies.active(request).rules()).restrict(new Condition.Equal("namespace",namespace))
            .restrict(new Condition.Not(new Condition.In("id",new ArrayList<>(references.referencedFileIds()))));
        var cb=em.getCriteriaBuilder();var q=cb.createTupleQuery();var root=q.from(FileMetadata.class);var fields=new TreeSet<>(FilePolicyService.RESOURCE.fields());
        q.multiselect(fields.stream().map(f->root.get(f).alias(f)).toArray(jakarta.persistence.criteria.Selection<?>[]::new));
        q.where(cb.equal(root.get("namespace"),namespace),cb.equal(root.get("id"),input.recordId()),cb.equal(root.get("state"),FileMetadata.State.READY));
        var rows=em.createQuery(q).getResultList();if(rows.isEmpty())return new Result(decision.allowed(),false,decision.eligibleRoles(),Map.of());
        Map<String,Object> values=new HashMap<>();fields.forEach(f->values.put(f,rows.get(0).get(f)));
        Map<String,Field> result=new TreeMap<>();fields.forEach(f->result.put(f,new Field(plan.canRead(values,f),plan.canWrite(values,f))));
        return new Result(decision.allowed(),plan.canAccess(values),decision.eligibleRoles(),result);
    }
}
