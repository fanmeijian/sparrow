package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = TablePrefix.NAME + "tx_cos_file")
public class TxCosFile extends BaseFile {
    private String fileName;
    private String bucket;
    private String region;
}
