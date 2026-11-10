package cn.sparrowmini.system.dictionary;

import jakarta.persistence.*;
@Entity @Table(name="spr_dictionary_entry",uniqueConstraints=@UniqueConstraint(columnNames={"dictionary_id","code"}))
public class DictionaryEntry {
    @Id @Column(length=36) String id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="dictionary_id",nullable=false) DictionaryDefinition dictionary;
    @Column(nullable=false,length=100) String code;
    @Column(nullable=false,length=200) String label;
    @Column(name="sort_order",nullable=false) int sortOrder;
    @Column(nullable=false) boolean enabled;
    @Version long version;
    public DictionaryEntry() { }
}
