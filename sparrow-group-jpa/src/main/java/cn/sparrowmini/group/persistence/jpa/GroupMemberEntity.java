package cn.sparrowmini.group.persistence.jpa;

import jakarta.persistence.*;
@Entity @Table(name="spr_group_member",uniqueConstraints=@UniqueConstraint(columnNames={"namespace","group_id","subject_id"}))
public class GroupMemberEntity {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(name="group_id",nullable=false,length=300) String groupId;
    @Column(name="subject_id",nullable=false,length=300) String subjectId;
    public GroupMemberEntity() { }
}
