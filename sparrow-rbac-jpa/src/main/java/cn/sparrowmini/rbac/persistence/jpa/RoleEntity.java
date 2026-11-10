package cn.sparrowmini.rbac.persistence.jpa;

import jakarta.persistence.*;
@Entity @Table(name="spr_role",uniqueConstraints=@UniqueConstraint(columnNames={"namespace","code"}))
public class RoleEntity {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(nullable=false,length=100) String code;
    @Column(nullable=false,length=200) String name;
    @Column(nullable=false) boolean enabled;
    @Version long version;
    public RoleEntity() { }
}
