package cn.sparrowmini.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Embeddable
public class ModelAttributeId implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(length = 500)
    private String modelId;

    private String attributeId;

    public ModelAttributeId(String modelId, String attributeId) {
        this.modelId = modelId;
        this.attributeId = attributeId;
    }

}