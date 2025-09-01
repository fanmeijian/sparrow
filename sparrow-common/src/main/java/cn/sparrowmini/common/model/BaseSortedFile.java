package cn.sparrowmini.common.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseSortedFile {
    protected int seq;
    protected String fileId;
    protected String name;
    protected long size;
}
