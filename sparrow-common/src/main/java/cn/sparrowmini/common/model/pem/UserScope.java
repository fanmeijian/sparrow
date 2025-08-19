package cn.sparrowmini.common.model.pem;

import cn.sparrowmini.common.model.BaseEntity;
import cn.sparrowmini.common.model.Scope;
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
@Table(name = TablePrefix.NAME + "user_scope")
public class UserScope extends BaseEntity implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JoinColumns({@JoinColumn(name = "username", insertable = false, updatable = false),
            @JoinColumn(name = "scopeId", insertable = false, updatable = false)})
    private UserScopeId id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "scopeId", insertable = false, updatable = false)
    private Scope scope;

    public UserScope(String username, String scopeId) {
        this.id = new UserScopeId(username, scopeId);
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class UserScopeId implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String username;
        private String scopeId;


        public UserScopeId(String username, String scopeId) {
            this.username = username;
            this.scopeId = scopeId;
        }

    }
}
