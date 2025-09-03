package cn.sparrowmini.rules.model;


import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = TablePrefix.NAME + "drl_template")
@NoArgsConstructor
public class DrlTemplate extends BaseUuidEntity implements Serializable {
    private String name;
    private String code;
    private String remark;

    @Column(columnDefinition = "TEXT")
    private String head;

}
