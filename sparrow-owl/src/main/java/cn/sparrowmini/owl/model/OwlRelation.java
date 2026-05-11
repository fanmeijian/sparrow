package cn.sparrowmini.owl.model;

import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = TablePrefix.NAME + "owl_relation")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "owl_type", discriminatorType = DiscriminatorType.STRING)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Setter
@Getter
@NoArgsConstructor
public class OwlRelation {
    @Id
    private String id;
    private OwlRelationObjectType sourceType;
    private String sourceCode;
    private OwlRelationObjectType targetType;
    private String targetCode;
    private OwlRelationType relationType;
    private OwlRelationOriginType origin;
}
