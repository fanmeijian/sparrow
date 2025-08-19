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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "user_model_attribute")
public class UserModelAttribute implements Serializable {
    public UserModelAttribute(ModelAttributeId attributeId, String username, PermissionTypeEnum permissionType,
                              PermissionEnum permission) {
        this.id = new UserModelAttributeId(attributeId, username, permissionType, permission);
    }

    private static final long serialVersionUID = 1L;
    @Id
    private UserModelAttributeId id;


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class UserModelAttributeId implements Serializable {
        private static final long serialVersionUID = 1L;
        private ModelAttributeId attributeId;
        private String username;

        @Column(length = 10)
        @Enumerated(EnumType.STRING)
        private PermissionTypeEnum permissionType;

        @Column(length = 30)
        @Enumerated(EnumType.STRING)
        private PermissionEnum permission;

        public UserModelAttributeId(ModelAttributeId attributeId, String username, PermissionTypeEnum permissionType,
                                    PermissionEnum permission) {
            super();
            this.attributeId = attributeId;
            this.username = username;
            this.permissionType = permissionType;
            this.permission = permission;
        }


    }
}
