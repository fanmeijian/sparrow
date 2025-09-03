package cn.sparrowmini.rules.model;

import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = TablePrefix.NAME + "drl_template_rule")
@NoArgsConstructor
public class DrlTemplateRule extends BaseUuidEntity implements Serializable {
    private String name;
    private String description;

    @Lob
    private String content;
    private String drlTemplateId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drlTemplateId", insertable = false, updatable = false)
    private DrlTemplate drlTemplate;
}
