package cn.sparrowmini.common.model.pem;


import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.model.Model;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = TablePrefix.NAME + "user_model")
public class UserModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private UserModelId id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "modelId", insertable = false, updatable = false)
    private Model model;

    public UserModel(String modelId, String username, PermissionTypeEnum permissionType, PermissionEnum permission) {
        this.id = new UserModelId(modelId, username, permissionType, permission);
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class UserModelId implements Serializable {
        private static final long serialVersionUID = 1L;

        @Column(length = 50)
        private String modelId;

        private String username;

        @Column(length = 10)
        @Enumerated(EnumType.STRING)
        private PermissionTypeEnum permissionType;

        @Column(length = 30)
        @Enumerated(EnumType.STRING)
        private PermissionEnum permission;

        public UserModelId(String modelId, String username, PermissionTypeEnum permissionType, PermissionEnum permission) {
            this.modelId = modelId;
            this.username = username;
            this.permissionType = permissionType;
            this.permission = permission;
        }
    }
}
