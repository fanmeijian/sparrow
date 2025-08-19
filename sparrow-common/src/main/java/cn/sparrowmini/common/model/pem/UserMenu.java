package cn.sparrowmini.common.model.pem;

import cn.sparrowmini.common.model.BaseEntity;
import cn.sparrowmini.common.model.Menu;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "user_menu")
public class UserMenu extends BaseEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @EqualsAndHashCode.Include
    @EmbeddedId
    private UserMenuId id;

    @ManyToOne
    @JoinColumn(name = "menuId", insertable = false, updatable = false)
    private Menu menu;

    // 是否包含所有子单，如果勾选了，则如果有新加子菜单，则会自动授予该用户
    private Boolean includeSubMenu = false;

    public UserMenu(UserMenuId id) {
        super();
        this.id = id;
    }

    public UserMenu(String menuId, String username) {
        super();
        this.id = new UserMenuId(username, menuId);
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class UserMenuId implements Serializable {
        // default serial version id, required for serializable classes.
        private static final long serialVersionUID = 1L;

        private String username;
        private String menuId;

        public UserMenuId(String username, String menuId) {
            this.username = username;
            this.menuId = menuId;
        }

    }

}
