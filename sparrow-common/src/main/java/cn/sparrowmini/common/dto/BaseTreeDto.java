package cn.sparrowmini.common.dto;

import cn.sparrowmini.common.model.BaseTree;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link BaseTree}
 */
@Getter
public class BaseTreeDto implements Serializable {
    String id;
    String parentId;
    String name;
    String code;
    String catalogId;
    Boolean includeAllChildren;
    String description;
    BigDecimal seq;

    @Setter
    @Transient
    long childCount=0;

    public BaseTreeDto(String id, String parentId, String name, String code, String catalogId, Boolean includeAllChildren, String description, BigDecimal seq) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.code = code;
        this.catalogId = catalogId;
        this.includeAllChildren = includeAllChildren;
        this.description = description;
        this.seq = seq;
    }
}