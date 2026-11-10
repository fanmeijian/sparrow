package cn.sparrowmini.system.navigation;

import jakarta.persistence.*;
@Entity @Table(name="spr_navigation_document")
public class NavigationDocument {
    @Id @Column(length=100) String namespace;
    @Lob @Column(nullable=false) String document;
    @Version long version;
    public NavigationDocument() { }
}
