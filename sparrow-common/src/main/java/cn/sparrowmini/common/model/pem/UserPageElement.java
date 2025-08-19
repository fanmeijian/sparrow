package cn.sparrowmini.common.model.pem;

import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.model.BaseEntity;
import cn.sparrowmini.common.model.PageElement;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "user_page_element")
public class UserPageElement extends BaseEntity {

    @EmbeddedId
    private UserPageElementId id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "pageElementId", insertable = false, updatable = false)
    private PageElement pageElement;

    public UserPageElement(UserPageElementId id) {
        this.id = id;
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class UserPageElementId implements Serializable {
        private String username;
        private String pageElementId;
        @Enumerated(EnumType.STRING)
        private PermissionTypeEnum type;

        public UserPageElementId(String username, String pageElementId, PermissionTypeEnum type) {
            this.username = username;
            this.pageElementId = pageElementId;
            this.type = type;
        }

    }
}
