package cn.sparrowmini.rules.model;

import cn.sparrowmini.common.model.BaseDataUuidEntity;
import cn.sparrowmini.common.model.BaseUuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Drl extends BaseUuidEntity {
    private String name;
    private String code;
    private String remark;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String content;

}
