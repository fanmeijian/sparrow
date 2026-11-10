package cn.sparrowmini.group.persistence.jpa;

import jakarta.persistence.*;
@Entity @Table(name="spr_group_edge",uniqueConstraints=@UniqueConstraint(columnNames={"namespace","parent_id","child_id"}))
public class GroupEdgeEntity {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(name="parent_id",nullable=false,length=300) String parentId;
    @Column(name="child_id",nullable=false,length=300) String childId;
    public GroupEdgeEntity() { }
}
