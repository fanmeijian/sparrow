package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.datapolicy.core.*;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;

public class DataPlanFactory {
    private final EntityManager em;private final FunctionPermissionEvaluator functions;private final JpaPolicyStore policies;
    private final DataPolicyRegistration registration;private final Map<String,DataPolicyExtension> extensions;
    public DataPlanFactory(EntityManager em,FunctionPermissionEvaluator functions,JpaPolicyStore policies,DataPolicyRegistration registration,Collection<DataPolicyExtension> extensions){
        this.em=Objects.requireNonNull(em);this.functions=Objects.requireNonNull(functions);this.policies=Objects.requireNonNull(policies);this.registration=Objects.requireNonNull(registration);
        Map<String,DataPolicyExtension> installed=new HashMap<>();for(var extension:extensions)if(extension.code()==null||extension.code().isBlank()||installed.put(extension.code(),extension)!=null)throw new IllegalArgumentException("Duplicate or invalid extension");
        for(var required:registration.requiredExtensions().values())if(!installed.keySet().containsAll(required))throw new IllegalArgumentException("Required data extension is not installed");this.extensions=Map.copyOf(installed);
        registration.resources().values().forEach(resource->resource.validate(em));
    }
    @Transactional(readOnly=true) public DataAccessPlan plan(ActorContext actor,PermissionRequest permission){
        require(permission);var decision=functions.evaluate(actor,permission);if(!decision.allowed())throw new SecurityException("Function permission denied");
        var snapshot=policies.active(permission);var plan=new DataAccessPlan(decision,snapshot.rules(),snapshot.activeRevision());
        for(String code:new TreeSet<>(registration.requiredExtensions().getOrDefault(permission,Set.of())))plan=plan.restrict(Objects.requireNonNull(extensions.get(code).restrict(decision.actor(),permission),"Extension returned no constraint"));return plan;
    }
    @Transactional(readOnly=true) public Page<Map<String,Object>> read(ActorContext actor,PermissionRequest permission,Condition filter,Pageable page,Set<String> fields){return reader(permission).read(plan(actor,permission),filter,page,fields);}
    private SecuredJpaReader<?> reader(PermissionRequest permission){return new SecuredJpaReader<>(em,require(permission));}
    private JpaResource<?> require(PermissionRequest permission){var resource=registration.resources().get(permission);if(resource==null)throw new IllegalArgumentException("No framework execution schema for permission");return resource;}
}
