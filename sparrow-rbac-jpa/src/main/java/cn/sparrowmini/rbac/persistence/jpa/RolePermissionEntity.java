package cn.sparrowmini.rbac.persistence.jpa;

import jakarta.persistence.*;
import cn.sparrowmini.security.api.Effect;
@Entity @Table(name="spr_role_permission",uniqueConstraints=@UniqueConstraint(columnNames={"namespace","role_id","resource_code","scope_code"}))
public class RolePermissionEntity {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(name="role_id",nullable=false,length=300) String roleId;
    @Column(name="resource_code",nullable=false,length=200) String resource;
    @Column(name="scope_code",nullable=false,length=100) String scope;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) Effect effect;
    public RolePermissionEntity() { }
}
