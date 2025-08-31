package cn.sparrowmini.rules.model;

import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = TablePrefix.NAME + "dslr")
public class Dslr extends BaseUuidEntity {
    private String name;
    private String content;
    private String dslId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "dslId", insertable = false, updatable = false)
    private Dsl dsl;
}
