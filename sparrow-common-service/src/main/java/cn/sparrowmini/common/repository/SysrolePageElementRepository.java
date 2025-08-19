package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.pem.SysrolePageElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface SysrolePageElementRepository extends JpaRepository<SysrolePageElement, SysrolePageElement.SysrolePageElementId> {
    long countByIdPageElementId(String pageElementId);

    @Query("select count(p) from SysrolePageElement p where p.id.pageElementId=:pageElementId and p.id.type='ALLOW'")
    long allowCount(String pageElementId);

    @Query("select p from SysrolePageElement p where p.id.pageElementId in (:elementIds) and p.id.sysroleId in (:roles)")
    List<SysrolePageElement> getByElementId(Collection<String> elementIds, Collection<String> roles);

}
