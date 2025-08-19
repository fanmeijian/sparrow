package cn.sparrowmini.common.model.pem;

import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.model.BaseEntity;
import cn.sparrowmini.common.model.PageElement;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "sysrole_page_element")
public class SysrolePageElement extends BaseEntity {

    @EmbeddedId
    private SysrolePageElementId id;

    @Transient
    private String sysroleName;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "pageElementId", insertable = false, updatable = false)
    private PageElement pageElement;


    public SysrolePageElement(SysrolePageElementId id) {
        this.id = id;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Embeddable
    public static class SysrolePageElementId implements Serializable {
        private String sysroleId;
        private String pageElementId;
        @Enumerated(EnumType.STRING)
        private PermissionTypeEnum type;

        public SysrolePageElementId(String sysroleId, String pageElementId, PermissionTypeEnum type) {
            this.sysroleId = sysroleId;
            this.pageElementId = pageElementId;
            this.type = type;
        }

    }
}
