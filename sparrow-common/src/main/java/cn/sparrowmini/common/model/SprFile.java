package cn.sparrowmini.common.model;

import cn.sparrowmini.common.constant.StorageTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
