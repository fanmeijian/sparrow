package cn.sparrowmini.bpm.server.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
public class TextFile extends BaseOpLog {
    @Id
    @GenericGenerator(name = "id-generator", strategy = "uuid")
    @GeneratedValue(generator = "id-generator")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @Lob
    @Column(columnDefinition = "TEXT") // 可选：PostgreSQL/MySQL 可用 columnDefinition="json"
    @Convert(converter = JsonArrayConverter.class)
    private List<Object> schema;
}
