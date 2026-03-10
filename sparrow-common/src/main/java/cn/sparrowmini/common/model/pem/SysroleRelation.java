package cn.sparrowmini.common.model.pem;

import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 系统角色关联表
 */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class SysroleRelation<T,ID> extends BaseUuidEntity {
    private ID businessId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "businessId", insertable = false, updatable = false)
    private T businessObject;

    private String sysroleId;

    @JoinColumn(name = "sysroleId", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Sysrole sysrole;
}
