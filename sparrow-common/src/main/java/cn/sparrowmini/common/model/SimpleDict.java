package cn.sparrowmini.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;

/**
 * 简易的字典，用于按实体区分
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "simple_dict")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "entityType", discriminatorType = DiscriminatorType.STRING)
public class SimpleDict extends BaseUuidEntity implements Serializable {

    @Column(insertable = false, updatable = false)
    private String entityType;

    private String name;
    private String code;
}
