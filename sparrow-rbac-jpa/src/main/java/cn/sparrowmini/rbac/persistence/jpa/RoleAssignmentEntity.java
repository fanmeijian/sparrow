package cn.sparrowmini.rbac.persistence.jpa;

import jakarta.persistence.*;
import cn.sparrowmini.security.api.Subject;
@Entity @Table(name="spr_role_assignment",uniqueConstraints=@UniqueConstraint(columnNames={"role_id","subject_kind","subject_id"}))
public class RoleAssignmentEntity {
    @Id @Column(length=36) String id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="role_id",nullable=false) RoleEntity role;
    @Enumerated(EnumType.STRING) @Column(name="subject_kind",nullable=false,length=20) Subject.Kind kind;
    @Column(name="subject_id",nullable=false,length=300) String subjectId;
    public RoleAssignmentEntity() { }
}
