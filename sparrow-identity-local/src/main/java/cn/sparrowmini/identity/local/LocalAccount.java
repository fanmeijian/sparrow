package cn.sparrowmini.identity.local;

import jakarta.persistence.*;
@Entity
@Table(name="spr_identity_account",uniqueConstraints=@UniqueConstraint(name="uk_account_login",columnNames={"namespace","login_name"}))
public class LocalAccount {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(name="login_name",nullable=false,length=150) String username;
    @Column(name="display_name",nullable=false,length=200) String displayName;
    @Column(name="password_hash",nullable=false,length=255) String passwordHash;
    @Column(nullable=false) boolean enabled;
    @Column(name="credential_version",nullable=false) long credentialVersion;
    @Version long version;
    public LocalAccount() { }
}
