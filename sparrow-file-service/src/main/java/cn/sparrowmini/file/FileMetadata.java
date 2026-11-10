package cn.sparrowmini.file;

import jakarta.persistence.*;
@Entity @Table(name="spr_file_object")
public class FileMetadata {
    public enum State { READY, DELETE_PENDING, DELETED }
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(nullable=false,length=100) String provider;
    @Column(nullable=false,length=500) String locator;
    @Column(nullable=false,length=255) String name;
    @Column(nullable=false) long size;
    @Column(nullable=false,length=64) String sha256;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) State state;
    @Version long version;
    public FileMetadata() { }
}
