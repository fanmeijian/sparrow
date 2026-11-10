package cn.sparrowmini.datapolicy.persistence.jpa;

import jakarta.persistence.*;
@Entity @Table(name="spr_data_policy",uniqueConstraints=@UniqueConstraint(columnNames={"namespace","resource_code","scope_code"}))
public class PolicyDocument {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(name="resource_code",nullable=false,length=200) String resource;
    @Column(name="scope_code",nullable=false,length=100) String scope;
    @Lob @Column(name="draft_json",nullable=false) String draftJson;
    @Lob @Column(name="active_json") String activeJson;
    @Column(name="active_revision",nullable=false) long activeRevision;
    @Version long version;
    public PolicyDocument() { }
}
