package cn.sparrowmini.file;

import jakarta.persistence.*;
@Entity
@Table(name="spr_file_reference",uniqueConstraints=@UniqueConstraint(name="uk_file_reference",columnNames={"namespace","file_id","resource_code","record_id","slot"}))
public class AttachmentReference {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(name="file_id",nullable=false,length=36) String fileId;
    @Column(name="resource_code",nullable=false,length=100) String resource;
    @Column(name="record_id",nullable=false,length=200) String recordId;
    @Column(nullable=false,length=100) String slot;
    @Version long version;
    public AttachmentReference() { }
}
