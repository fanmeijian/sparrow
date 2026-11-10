package cn.sparrowmini.system.presentation;

import jakarta.persistence.*;
@Entity @Table(name="spr_ui_page",uniqueConstraints=@UniqueConstraint(name="uk_ui_page",columnNames={"namespace","code"}))
public class UiPage {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(nullable=false,length=100) String code;
    @Lob @Column(nullable=false) String document;
    @Version long version;
    public UiPage() { }
}
