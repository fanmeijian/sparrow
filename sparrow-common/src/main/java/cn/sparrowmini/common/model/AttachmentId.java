package cn.sparrowmini.common.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class AttachmentId<ID> implements Serializable {

    private ID businessObjectId;
    private String fileId;

    public AttachmentId(ID articleId, String fileId) {
        this.businessObjectId = articleId;
        this.fileId = fileId;
    }
}