package cn.sparrowmini.rbac.persistence.jpa.catalog;
import jakarta.persistence.*;
@Entity @Table(name="spr_permission_resource",uniqueConstraints=@UniqueConstraint(columnNames={"namespace","code"}))
public class PermissionResourceEntity {
 @Id public String id;
 @Column(nullable=false,length=100) public String namespace;
 @Column(nullable=false,length=200) public String code;
 @Column(nullable=false) public String name;
 public String description;
 @Column(nullable=false) public boolean enabled;
 @Version public long version;
}
