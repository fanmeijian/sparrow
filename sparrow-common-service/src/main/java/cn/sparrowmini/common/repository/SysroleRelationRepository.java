package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.pem.SysroleRelation;
import cn.sparrowmini.common.model.pem.SysroleRelation_;
import org.springframework.data.jpa.domain.Specification;

public interface SysroleRelationRepository<T extends SysroleRelation,ID> extends BaseRepository<T, String> {

    default boolean existsRelation(ID businessId, String sysroleId) {
        Specification<T> specification = specificationEqual(SysroleRelation_.BUSINESS_ID, businessId)
                .and(specificationEqual(SysroleRelation_.SYSROLE, sysroleId));
        return exists(specification);
    };
}
