package cn.sparrowmini.system.dictionary;

import jakarta.persistence.*;
@Entity @Table(name="spr_dictionary",uniqueConstraints=@UniqueConstraint(columnNames={"namespace","code"}))
public class DictionaryDefinition {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(nullable=false,length=100) String code;
    @Column(nullable=false,length=200) String name;
    @Version long version;
    public DictionaryDefinition() { }
}
