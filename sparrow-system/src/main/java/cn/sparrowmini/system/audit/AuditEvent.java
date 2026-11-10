package cn.sparrowmini.system.audit;

import jakarta.persistence.*;
@Entity @Table(name="spr_management_audit",indexes=@Index(name="ix_audit_namespace_time",columnList="namespace,occurred_at"))
public class AuditEvent {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(nullable=false,length=300) String subject;
    @Column(nullable=false,length=12) String operation;
    @Column(nullable=false,length=500) String target;
    @Column(name="occurred_at",nullable=false) long occurredAt;
    @Column(nullable=false,length=30) String outcome;
    @Column(name="request_id",nullable=false,length=36) String requestId;
    @Column(name="policy_revision") Long policyRevision;
    @Column(nullable=false,length=100) String reason;
    public AuditEvent() { }
}
