package cn.sparrowmini.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "spr_dict")
public class Dict extends BaseTree implements Serializable {
    private String catalogId;

    @Transient
    private long childCount = 0;

    public Dict(String name, String code, String catalogId, String parentId) {
        super();
        this.setName(name);
        this.setCode(code);
        this.catalogId = catalogId;
        this.setParentId(parentId);
    }

}
