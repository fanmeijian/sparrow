package cn.sparrowmini.rbac.persistence.jpa.catalog;
import jakarta.persistence.*;
@Entity @Table(name="spr_permission_scope",uniqueConstraints=@UniqueConstraint(columnNames={"resource_id","code"}))
public class PermissionScopeEntity {
 @Id public String id;
 @ManyToOne(optional=false) @JoinColumn(name="resource_id",nullable=false) public PermissionResourceEntity resource;
 @Column(nullable=false,length=100) public String code;
 @Column(nullable=false) public String name;
 public String description;
 @Column(nullable=false) public boolean enabled;
 @Version public long version;
}
