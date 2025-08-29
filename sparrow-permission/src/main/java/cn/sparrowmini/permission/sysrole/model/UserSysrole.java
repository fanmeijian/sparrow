package cn.sparrowmini.permission.sysrole.model;

import cn.sparrowmini.common.model.BaseEntity;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = TablePrefix.NAME +"user_sysrole")
//@NamedQueries({
//        @NamedQuery(name = "UserSysrole.findByUsername", query = "SELECT o FROM UserSysrole o WHERE o.id.username = :username")})
public class UserSysrole extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private UserSysroleId id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "sysroleId", insertable = false, updatable = false)
    private Sysrole sysrole;

    public UserSysrole(UserSysroleId id) {
        this.id = id;
    }

    public UserSysrole(String sysroleId, String username) {
        this.id = new UserSysroleId(username, sysroleId);
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class UserSysroleId implements Serializable {
        // default serial version id, required for serializable classes.
        private static final long serialVersionUID = 1L;

        private String username;
        private String sysroleId;

        public UserSysroleId(String username, String sysroleId) {
            this.username = username;
            this.sysroleId = sysroleId;
        }

    }

}
