package cn.sparrowmini.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.util.Set;

/**
 * 文件的基类
 */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseFile extends BaseOpLog implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue
    @UuidGenerator
    protected String id;

    protected int seq;
    protected String path;
    protected String name;
    protected long size;
    protected String hash;
    protected String type;
    @Column(length = 1000)
    protected String url;

    private String fileName;

    @ElementCollection
    protected Set<String> catalog;

    @PreUpdate
    @PrePersist
    public void preSave(){
        String ext = FilenameUtils.getExtension(this.name);
        this.fileName = String.join("." ,this.hash,ext);
    }
}
