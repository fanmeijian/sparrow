package cn.sparrowmini.tree;

import jakarta.persistence.*;
@Entity @Table(name="spr_hierarchy")
public class HierarchyEntity {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(nullable=false,length=200) String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) HierarchyGraph.Mode mode;
    @Column(nullable=false) long revision;
    @Version long version;
    public HierarchyEntity() { }
}
