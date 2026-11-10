package cn.sparrowmini.tree;

import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="spr_hierarchy_node")
public class HierarchyNodeEntity {
    @Id @Column(length=36) String id;
    @ManyToOne(optional=false) @JoinColumn(name="hierarchy_id",nullable=false) HierarchyEntity hierarchy;
    @Column(length=100) String resource;
    @Column(name="record_id",length=200) String recordId;
    @Column(name="root_order",nullable=false,precision=28,scale=8) BigDecimal rootOrder;
    public HierarchyNodeEntity() { }
}
