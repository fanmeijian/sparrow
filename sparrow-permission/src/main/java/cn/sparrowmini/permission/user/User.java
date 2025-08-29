package cn.sparrowmini.permission.user;

import cn.sparrowmini.common.model.BaseEntity;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "user")
public class User extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String username;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String mobile;

    @Column(unique = true)
    private String keycloakId;

    private String firstName;

    private String lastName;


    public User(String username) {
        super();
        this.username = username;
    }

    public User(String username, String keycloakId) {
        super();
        this.username = username;
        this.keycloakId = keycloakId;
    }


    public User(String username, String email, String mobile, String keycloakId, String firstName, String lastName, Boolean enabled) {
        super();
        this.username = username;
        this.email = email;
        this.mobile = mobile;
        this.keycloakId = keycloakId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.setEnabled(enabled);
    }


}
