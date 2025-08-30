package cn.sparrowmini.ext.file.local;

import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = TablePrefix.NAME + "local_file")
public class LocalFile extends BaseFile {
}
