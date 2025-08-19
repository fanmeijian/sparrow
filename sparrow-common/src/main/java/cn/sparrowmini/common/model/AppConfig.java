package cn.sparrowmini.common.model;

import cn.sparrowmini.common.converter.JsonArrayConverter;
import cn.sparrowmini.common.converter.JsonMapConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "app_config")
public class AppConfig extends BaseEntity {

    @Id
    private String code;
    private String name;
    private String description;

    @Lob
    @Convert(converter = JsonArrayConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Map<String, Object>> configJson; // 保存变量的 JSON 字符串

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER, mappedBy = "appConfig")
    private List<AppConfigAttachment> attachments;
}
