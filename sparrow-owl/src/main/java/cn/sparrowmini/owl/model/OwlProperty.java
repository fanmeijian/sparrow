package cn.sparrowmini.owl.model;

import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = TablePrefix.NAME + "owl_property", uniqueConstraints = @UniqueConstraint(columnNames = {"owl_type","code"}))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "owl_type", discriminatorType = DiscriminatorType.STRING)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Setter
@Getter
@NoArgsConstructor
public class OwlProperty extends BaseTree {

//    @Lob
//    @Convert(converter = JsonMapConverter.class)
//    private Map<String, Object> labels;

    private OwlPropertyTypeEnum type;
    private String range;
}
