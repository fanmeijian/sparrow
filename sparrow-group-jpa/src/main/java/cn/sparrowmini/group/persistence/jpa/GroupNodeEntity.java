package cn.sparrowmini.group.persistence.jpa;

import jakarta.persistence.*;
@Entity @Table(name="spr_group_node")
public class GroupNodeEntity {
    @Id @Column(length=300) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(nullable=false,length=200) String name;
    @Column(nullable=false) boolean enabled;
    public GroupNodeEntity() { }
}
