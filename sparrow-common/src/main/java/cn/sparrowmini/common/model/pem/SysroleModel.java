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
@Table(name = TablePrefix.NAME + "sysrole_model")
public class SysroleModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private SysroleModelId id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "modelId", insertable = false, updatable = false)
    private Model model;

    public SysroleModel(String modelId, String sysroleId, PermissionTypeEnum permissionType,
                        PermissionEnum permission) {
        this.id = new SysroleModelId(modelId, sysroleId, permissionType, permission);
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class SysroleModelId implements Serializable {
        private static final long serialVersionUID = 1L;
        @Column(length = 50)
        private String modelId;

        @Column(length = 50)
        private String sysroleId;

        @Column(length = 10)
        @Enumerated(EnumType.STRING)
        private PermissionTypeEnum permissionType;

        @Column(length = 30)
        @Enumerated(EnumType.STRING)
        private PermissionEnum permission;

        public SysroleModelId(String modelId, String sysroleId, PermissionTypeEnum permissionType, PermissionEnum permission) {
            this.modelId = modelId;
            this.sysroleId = sysroleId;
            this.permissionType = permissionType;
            this.permission = permission;
        }

    }
}
