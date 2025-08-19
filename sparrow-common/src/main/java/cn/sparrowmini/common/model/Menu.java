package cn.sparrowmini.common.model;

import cn.sparrowmini.common.model.pem.SysroleMenu;
import cn.sparrowmini.common.model.pem.UserMenu;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "menu")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Menu extends BaseTree implements Serializable {


    private String url;
    private Boolean isSystem;
    private String icon;
    private String target;
    private String type;
    private String queryParams;

    @JsonIgnore
    @OneToMany(mappedBy = "menu")
    private Set<UserMenu> userMenus;

    public Menu(String code, String parentId) {
        this.code = code;
        this.parentId = parentId;
    }

    public Menu(String name, String code, String parentId, String url, String icon) {
        this.name = name;
        this.code = code;
        this.parentId = parentId;
        this.url = url;
        this.icon = icon;
    }

}
