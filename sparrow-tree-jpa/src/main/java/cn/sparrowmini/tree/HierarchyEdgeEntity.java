package cn.sparrowmini.tree;

import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="spr_hierarchy_edge",uniqueConstraints=@UniqueConstraint(name="uk_hierarchy_edge",columnNames={"hierarchy_id","parent_id","child_id"}))
public class HierarchyEdgeEntity {
    @Id @Column(length=36) String id;
    @ManyToOne(optional=false) @JoinColumn(name="hierarchy_id",nullable=false) HierarchyEntity hierarchy;
    @ManyToOne(optional=false) @JoinColumn(name="parent_id",nullable=false) HierarchyNodeEntity parent;
    @ManyToOne(optional=false) @JoinColumn(name="child_id",nullable=false) HierarchyNodeEntity child;
    @Column(name="sort_order",nullable=false,precision=28,scale=8) BigDecimal sortOrder;
    public HierarchyEdgeEntity() { }
}
