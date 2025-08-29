package cn.sparrowmini.permission.file.model;

import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.io.Serializable;
import java.util.Set;

/**
 * 文件对象
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "file")
public class SparrowFile extends BaseFile implements Serializable {

    @ElementCollection
    @JoinTable(name = TablePrefix.NAME + "file_catalog")
    private Set<String> catalog;



    public SparrowFile(String path, String name, long size, String hash, String fileName) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.hash = hash;
        this.fileName = fileName;
    }
}