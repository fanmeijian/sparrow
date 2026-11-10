package cn.sparrowmini.group.persistence.jpa;

import jakarta.persistence.*;
@Entity @Table(name="spr_group_namespace")
public class GroupNamespace {
    @Id @Column(length=100) String id;
    @Column(nullable=false) long revision;
    @Version long version;
    public GroupNamespace() { }
}
