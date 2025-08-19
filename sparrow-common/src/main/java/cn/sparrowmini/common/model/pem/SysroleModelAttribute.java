package cn.sparrowmini.common.model.pem;

import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.model.ModelAttributeId;
import cn.sparrowmini.common.model.TablePrefix;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "sysrole_model_attribute")
public class SysroleModelAttribute implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private SysroleModelAttributeId id;

    public SysroleModelAttribute(ModelAttributeId attributeId, String sysroleId, PermissionTypeEnum permissionType, PermissionEnum permission) {
        this.id = new SysroleModelAttributeId(attributeId, sysroleId, permissionType, permission);
    }

    @Data
    @NoArgsConstructor
    @Embeddable
    public static class SysroleModelAttributeId implements Serializable {
        private static final long serialVersionUID = 1L;
        private ModelAttributeId attributeId;

        @Column(length = 50)
        private String sysroleId;

        @Column(length = 10)
        @Enumerated(EnumType.STRING)
        private PermissionTypeEnum permissionType;

        @Column(length = 30)
        @Enumerated(EnumType.STRING)
        private PermissionEnum permission;


        public SysroleModelAttributeId(ModelAttributeId attributeId, String sysroleId, PermissionTypeEnum permissionType, PermissionEnum permission) {
            this.attributeId = attributeId;
            this.sysroleId = sysroleId;
            this.permissionType = permissionType;
            this.permission = permission;
        }

    }
}
