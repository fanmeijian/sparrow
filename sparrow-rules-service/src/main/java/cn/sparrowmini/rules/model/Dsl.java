package cn.sparrowmini.rules.model;


import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * dsl 模板
 */

@Getter
@Setter
@Entity
@Table(name = TablePrefix.NAME + "dsl")
public class Dsl extends BaseUuidEntity {
    private String name;
    private String code;
    private String remark;

    @Lob
    private String content;

}
