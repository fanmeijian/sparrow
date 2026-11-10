package cn.sparrowmini.file;

import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** References are changed under the same file-row lock used by deletion. */
public class FileReferenceService implements FileService.ReferenceCheck {
    public record Reference(String id,String fileId,String resource,String recordId,String slot,long version) { }
    @FunctionalInterface public interface Guard {void require(String operation,String fileId,String resource,String recordId);}
    private final EntityManager em;private final String namespace;private final Guard guard;
    public FileReferenceService(EntityManager em,String namespace,Guard guard){this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);}
    @Transactional public Reference attach(String fileId,String resource,String recordId,String slot){
        valid(resource,100);valid(recordId,200);valid(slot,100);guard.require("attach",fileId,resource,recordId);var file=lock(fileId);
        if(file.state!=FileMetadata.State.READY)throw new IllegalStateException("File unavailable");
        for(var existing:rows(fileId))if(existing.resource.equals(resource)&&existing.recordId.equals(recordId)&&existing.slot.equals(slot))return view(existing);
        var r=new AttachmentReference();r.id=UUID.randomUUID().toString();r.namespace=namespace;r.fileId=fileId;r.resource=resource;r.recordId=recordId;r.slot=slot;em.persist(r);em.flush();return view(r);
    }
    @Transactional public void detach(String fileId,String id,long version){
        lock(fileId);var reference=em.find(AttachmentReference.class,id,LockModeType.PESSIMISTIC_WRITE);
        if(reference==null || !namespace.equals(reference.namespace) || !fileId.equals(reference.fileId))throw new IllegalArgumentException("Reference not found");
        em.refresh(reference);guard.require("detach",fileId,reference.resource,reference.recordId);
        if(reference.version!=version)throw new OptimisticLockException("Stale reference version");em.remove(reference);em.flush();
    }
    @Transactional(readOnly=true) public List<Reference> list(String fileId){guard.require("read",fileId,null,null);return rows(fileId).stream().map(this::view).toList();}
    /** Internal integrity port. Caller holds the file-row lock before deletion. */
    @Transactional(readOnly=true) public boolean isUnreferenced(String fileId){
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(Long.class);var r=q.from(AttachmentReference.class);
        q.select(cb.count(r)).where(cb.equal(r.get("namespace"),namespace),cb.equal(r.get("fileId"),fileId));return em.createQuery(q).getSingleResult()==0;
    }
    private List<AttachmentReference> rows(String id){var cb=em.getCriteriaBuilder();var q=cb.createQuery(AttachmentReference.class);var r=q.from(AttachmentReference.class);q.select(r).where(cb.equal(r.get("namespace"),namespace),cb.equal(r.get("fileId"),id)).orderBy(cb.asc(r.get("id")));return em.createQuery(q).getResultList();}
    private FileMetadata lock(String id){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");var f=em.find(FileMetadata.class,id,LockModeType.PESSIMISTIC_WRITE);if(f==null||!namespace.equals(f.namespace))throw new IllegalArgumentException("File not found");em.refresh(f);return f;}
    private Reference view(AttachmentReference r){return new Reference(r.id,r.fileId,r.resource,r.recordId,r.slot,r.version);}
    private void valid(String value,int max){if(value==null||value.isBlank()||value.length()>max)throw new IllegalArgumentException("Invalid reference");}
}
