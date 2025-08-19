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
@Table(name = TablePrefix.NAME + "sysrole_scope")
public class SysroleScope extends BaseEntity implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JoinColumns({@JoinColumn(name = "sysroleId", insertable = false, updatable = false),
            @JoinColumn(name = "scopeId", insertable = false, updatable = false)})
    private SysroleScopeId id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "scopeId", insertable = false, updatable = false)
    private Scope scope;

    public SysroleScope(String sysroleId, String scopeId) {
        this.id = new SysroleScopeId(sysroleId, scopeId);
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class SysroleScopeId implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String sysroleId;
        private String scopeId;

        public SysroleScopeId(String sysroleId, String scopeId) {
            this.sysroleId = sysroleId;
            this.scopeId = scopeId;
        }

    }
}
