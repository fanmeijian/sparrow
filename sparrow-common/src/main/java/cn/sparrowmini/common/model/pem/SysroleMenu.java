package cn.sparrowmini.common.model.pem;

import cn.sparrowmini.common.model.BaseEntity;
import cn.sparrowmini.common.model.Menu;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "sysrole_menu")
@NamedQuery(name = "SysroleMenu.findAll", query = "SELECT s FROM SysroleMenu s")
public class SysroleMenu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JoinColumns({@JoinColumn(name = "sysroleId"), @JoinColumn(name = "menuId"),})
    private SysroleMenuId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menuId", insertable = false, updatable = false)
    private Menu menu;


    // 是否包含所有子单，如果勾选了，则如果有新加子菜单，则会自动授予该角色
    private Boolean includeSubMenu = false;

    public SysroleMenu(SysroleMenuId sysroleMenuId) {
        this.id = sysroleMenuId;
    }

    public SysroleMenu(String menuId, String sysroleId) {
        this.id = new SysroleMenuId(sysroleId, menuId);
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class SysroleMenuId implements Serializable {
        // default serial version id, required for serializable classes.
        private static final long serialVersionUID = 1L;

        private String sysroleId;

        private String menuId;

        public SysroleMenuId(String sysroleId, String menuId) {
            this.sysroleId = sysroleId;
            this.menuId = menuId;
        }

    }

}