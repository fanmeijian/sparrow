package cn.sparrowmini.common.model;

import cn.sparrowmini.common.model.pem.SysroleScope;
import cn.sparrowmini.common.model.pem.UserScope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

/*
 * 功能权限表，相当于oauth2的scope范围，可用于细粒度的控制
 *
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "scope")
public class Scope extends BaseUuidEntity implements Serializable {


    private String name;
    @Column(unique = true, nullable = false)
    private String code; // app:module:action
//
//    @OneToOne
//    @JoinColumn(name = "typeId", insertable = false, updatable = false)
//    private ScopeCatalog type;

    private String typeId;

    private String permissionTokenId;


//    @JsonIgnore
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "scope", fetch = FetchType.EAGER)
    private Set<SysroleScope> sysroleScopes;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "scope", fetch = FetchType.EAGER)
    private Set<UserScope> userScopes;

    public Scope(String name, String code) {
        this.name = name;
        this.code = code;
    }

}
