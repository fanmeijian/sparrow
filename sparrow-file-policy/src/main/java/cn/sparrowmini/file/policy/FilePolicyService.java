package cn.sparrowmini.file.policy;

import cn.sparrowmini.file.*;
import cn.sparrowmini.storage.api.StorageRegistry;
import cn.sparrowmini.security.api.*;
import cn.sparrowmini.datapolicy.core.*;
import cn.sparrowmini.datapolicy.persistence.jpa.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.io.*;
import java.util.*;

/** Optional file access adapter. Additional project constraints are always ANDed with policy. */
public class FilePolicyService {
    @FunctionalInterface public interface AdditionalAccess {Condition restrict(ActorContext actor,PermissionRequest permission);}
    public static final JpaResource<FileMetadata> RESOURCE=new JpaResource<>("file.object",FileMetadata.class,"id",Set.of("id","name","size","sha256","namespace"));
    private final EntityManager em;private final String namespace;private final CurrentActorProvider actors;
    private final FunctionPermissionEvaluator functions;private final JpaPolicyStore policies;private final AdditionalAccess additional;
    private final FileService files;private final SecuredJpaReader<FileMetadata> reader;
    public FilePolicyService(EntityManager em,String namespace,CurrentActorProvider actors,FunctionPermissionEvaluator functions,JpaPolicyStore policies,AdditionalAccess additional,StorageRegistry storage,FileService.ReferenceCheck references){
        this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.actors=Objects.requireNonNull(actors);this.functions=Objects.requireNonNull(functions);this.policies=Objects.requireNonNull(policies);this.additional=Objects.requireNonNull(additional);
        reader=new SecuredJpaReader<>(em,RESOURCE,(r,q,cb)->cb.equal(r.get("state"),FileMetadata.State.READY));
        files=new FileService(em,storage,namespace,(n,scope,id,context)->{
            String action=switch(scope){case "download"->"download";case "update-metadata"->"update";case "delete"->"delete";default->throw new SecurityException("Unsupported file operation");};
            if(!plan(action).canAccess(facts(id)))throw new SecurityException("File outside permitted range");
        },references);
    }
    @Transactional(readOnly=true)
    public Page<Map<String,Object>> list(int page,int size,Set<String> fields){return reader.read(plan("read"),new Condition.All(),PageRequest.of(page,size),fields);}
    @Transactional(readOnly=true)
    public void download(String id,OutputStream output)throws IOException{files.download(id,null,output);}
    @Transactional
    public void rename(String id,long version,String name){
        var before=facts(id);var after=new HashMap<>(before);after.put("name",name);var plan=plan("update");
        if(!plan.canWrite(before,"name") || !plan.canWrite(after,"name"))throw new SecurityException("File name is not writable in both states");
        files.rename(id,version,name,null);
    }
    @Transactional public void requestDelete(String id,long version){files.requestDelete(id,version,null);}
    @Transactional(readOnly=true) public boolean capability(String scope,String id,String field,boolean write){
        if(id==null)return false;
        try{var plan=plan(scope);var row=facts(id);if(field==null)return plan.canAccess(row);RESOURCE.requireField(field);return write?plan.canWrite(row,field):plan.canRead(row,field);}
        catch(SecurityException denied){return false;}
    }
    private DataAccessPlan plan(String scope){
        var request=new PermissionRequest(RESOURCE.code(),scope);var actor=actors.current();var decision=functions.evaluate(actor,request);
        if(!decision.allowed())throw new SecurityException("Function permission denied");
        var snapshot=policies.active(request);return new DataAccessPlan(decision,snapshot.rules(),snapshot.activeRevision()).restrict(new Condition.Equal("namespace",namespace))
            .restrict(Objects.requireNonNull(additional.restrict(decision.actor(),request),"Additional constraint required"));
    }
    private Map<String,Object> facts(String id){
        var cb=em.getCriteriaBuilder();var q=cb.createTupleQuery();var root=q.from(FileMetadata.class);
        var fields=new TreeSet<>(RESOURCE.fields());q.multiselect(fields.stream().map(f->root.get(f).alias(f)).toArray(jakarta.persistence.criteria.Selection<?>[]::new));
        q.where(cb.equal(root.get("namespace"),namespace),cb.equal(root.get("id"),id),cb.equal(root.get("state"),FileMetadata.State.READY));
        // Write calls hold the lock before checking policy, so rename cannot race with authorization.
        var query=em.createQuery(q);if(!org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly())query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        var rows=query.getResultList();if(rows.isEmpty())throw new SecurityException("File unavailable");
        Map<String,Object> values=new HashMap<>();fields.forEach(f->values.put(f,rows.get(0).get(f)));return values;
    }
}
