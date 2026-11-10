package cn.sparrowmini.file;

import cn.sparrowmini.storage.api.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import java.io.*;
import java.util.*;

public class FileService {
    public record Info(String id,String name,long size,String sha256,FileMetadata.State state,long version) { }
    /** Host must check function, file range, parent binding and delegation for the supplied context. */
    @FunctionalInterface public interface Guard {void require(String namespace,String scope,String fileId,String businessContext);}
    @FunctionalInterface public interface ReferenceCheck {boolean isUnreferenced(String fileId);}
    private final EntityManager em;private final StorageRegistry storage;private final String namespace;private final Guard guard;private final ReferenceCheck references;
    public FileService(EntityManager em,StorageRegistry storage,String namespace,Guard guard,ReferenceCheck references){
        this.em=Objects.requireNonNull(em);this.storage=Objects.requireNonNull(storage);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);this.references=Objects.requireNonNull(references);
    }
    @Transactional(rollbackFor=IOException.class)
    public Info upload(String provider,String name,InputStream input,String context)throws IOException{
        guard.require(namespace,"upload",null,context);transaction();
        if(!TransactionSynchronizationManager.isSynchronizationActive())throw new IllegalStateException("Spring transaction synchronization required");
        validName(name);var backend=storage.require(provider);var object=backend.upload(input);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            public void afterCompletion(int status){if(status!=STATUS_COMMITTED)try{backend.delete(object.key());}catch(IOException e){System.getLogger(FileService.class.getName()).log(System.Logger.Level.ERROR,"Failed rollback object cleanup; reconcile storage inventory",e);}}
        });
        var f=new FileMetadata();f.id=UUID.randomUUID().toString();f.namespace=namespace;f.provider=provider;f.locator=object.key();f.name=name;f.size=object.size();f.sha256=object.sha256();f.state=FileMetadata.State.READY;
        em.persist(f);em.flush();return info(f);
    }
    @Transactional(readOnly=true) public Info get(String id,String context){guard.require(namespace,"read-metadata",id,context);return info(find(id,false));}
    @Transactional(readOnly=true) public List<Info> list(int offset,int limit,String context){
        guard.require(namespace,"list",null,context);if(offset<0 || limit<1 || limit>500)throw new IllegalArgumentException("Invalid pagination");
        // Listing is an explicit namespace-wide capability. Hosts requiring row-filtered lists use their secured query adapter.
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(FileMetadata.class);var root=q.from(FileMetadata.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace),cb.notEqual(root.get("state"),FileMetadata.State.DELETED)).orderBy(cb.asc(root.get("id")));
        return em.createQuery(q).setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(this::info).toList();
    }
    @Transactional(readOnly=true) public void download(String id,String context,OutputStream output)throws IOException{
        guard.require(namespace,"download",id,context);var f=find(id,false);if(f.state!=FileMetadata.State.READY)throw new IllegalStateException("File unavailable");
        try(var input=storage.require(f.provider).open(f.locator)){input.transferTo(output);}
    }
    @Transactional public Info rename(String id,long version,String name,String context){
        guard.require(namespace,"update-metadata",id,context);transaction();validName(name);var f=find(id,true);checkVersion(f,version);if(f.state!=FileMetadata.State.READY)throw new IllegalStateException("File unavailable");f.name=name;em.flush();return info(f);
    }
    @Transactional public Info requestDelete(String id,long version,String context){
        guard.require(namespace,"delete",id,context);transaction();var f=find(id,true);checkVersion(f,version);
        if(!references.isUnreferenced(id))throw new IllegalStateException("File is referenced or cannot be verified");
        if(f.state!=FileMetadata.State.DELETED)f.state=FileMetadata.State.DELETE_PENDING;em.flush();return info(f);
    }
    /** A retryable worker invokes this under a system identity. Deletion is idempotent. */
    @Transactional(rollbackFor=IOException.class) public Info completeDelete(String id,String context)throws IOException{
        guard.require(namespace,"complete-delete",id,context);transaction();var f=find(id,true);
        if(f.state==FileMetadata.State.DELETED)return info(f);
        if(f.state!=FileMetadata.State.DELETE_PENDING)throw new IllegalStateException("Deletion not requested");
        if(!references.isUnreferenced(id))throw new IllegalStateException("File gained references");
        storage.require(f.provider).delete(f.locator);f.state=FileMetadata.State.DELETED;em.flush();return info(f);
    }
    private FileMetadata find(String id,boolean lock){var f=lock?em.find(FileMetadata.class,id,LockModeType.PESSIMISTIC_WRITE):em.find(FileMetadata.class,id);if(f==null || !namespace.equals(f.namespace))throw new IllegalArgumentException("File not found");if(lock)em.refresh(f,LockModeType.PESSIMISTIC_WRITE);return f;}
    private Info info(FileMetadata f){return new Info(f.id,f.name,f.size,f.sha256,f.state,f.version);}
    private void transaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
    private void checkVersion(FileMetadata f,long version){if(f.version!=version)throw new OptimisticLockException("Stale file version");}
    private void validName(String name){if(name==null || name.isBlank() || name.length()>255 || name.chars().anyMatch(c->c<32))throw new IllegalArgumentException("Invalid file name");}
}
