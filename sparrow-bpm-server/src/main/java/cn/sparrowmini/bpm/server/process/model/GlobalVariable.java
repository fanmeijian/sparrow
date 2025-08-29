package cn.sparrowmini.bpm.server.process.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"containerId", "code"})})
@Getter
@Setter
@NoArgsConstructor
public class GlobalVariable implements Serializable {

    @Id
    @GenericGenerator(name = "id-generator", strategy = "uuid")
    @GeneratedValue(generator = "id-generator")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    private String containerId;
    private String code;
    private String name;
    private String value;
    private String type;

}
