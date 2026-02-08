package cn.sparrowmini.common.model;

import cn.sparrowmini.common.constant.StorageTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 将所有的文件上传集中在一起，以不同的存储方式隔离
 * @Entity
 * @DiscriminatorValue("TX_COS")
 * public class TxCosFile extends SprFile {
 *     private String bucket;
 *     private String region;
 * }
 *
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "file")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "storageType", discriminatorType = DiscriminatorType.STRING)
public class SprFile extends BaseFile {

    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private StorageTypeEnum storageType;
}
