package cn.sparrowmini.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseAttachment<F extends SprFile,T, ID> extends BaseState{
    private int seq=0;

    @EmbeddedId
    private AttachmentId<ID> id;

    @OneToOne
    @JoinColumn(name = "fileId", insertable = false, updatable = false)
    private F file;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "businessObjectId", updatable = false, insertable = false)
    private T businessObject;

    public BaseAttachment(ID businessObjectId, String fileId) {
        this.id = new AttachmentId<ID>(businessObjectId,fileId);
    }
}
