package cn.sparrowmini.system.configuration;

import jakarta.persistence.*;
@Entity @Table(name="spr_configuration_item",uniqueConstraints=@UniqueConstraint(name="uk_configuration_code",columnNames={"namespace","code"}))
public class ConfigurationItem {
    @Id @Column(length=36) String id;
    @Column(nullable=false,length=100) String namespace;
    @Column(nullable=false,length=100) String code;
    @Lob @Column(name="config_value",nullable=false) String value;
    @Version long version;
    public ConfigurationItem() { }
}
