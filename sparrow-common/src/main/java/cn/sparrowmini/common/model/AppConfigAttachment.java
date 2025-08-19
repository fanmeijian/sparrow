package cn.sparrowmini.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Base64;

/**
 * 应用配置的附件
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "app_config_attachment")
public class AppConfigAttachment extends BaseUuidEntity {

    @Transient
    private String content;

    @JsonIgnore
    @Lob
    private byte[] contentByte;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "appConfigId", insertable = false, updatable = false)
    private AppConfig appConfig;

    private String appConfigId;
    private String fileName;
    private long size;

    // 将 byte[] 转为 base64 字符串
    public String getContent() {
        if (contentByte != null) {
            return Base64.getEncoder().encodeToString(contentByte);
        }
        return null;
    }

    // 将 base64 字符串转为 byte[]
    public void setContent(String content) {
        this.content = content;
        if (content != null) {
            this.contentByte = Base64.getDecoder().decode(content);
        }
    }
}
