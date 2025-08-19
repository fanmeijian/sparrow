package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.pem.UserPageElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface UserPageElementRepository extends JpaRepository<UserPageElement, UserPageElement.UserPageElementId> {
    long countByIdPageElementId(String pageElementId);

    @Query("select count(p) from UserPageElement p where p.id.pageElementId = :pageElementId and p.id.type='ALLOW'")
    long allowCount(String pageElementId);

    @Query("select p from UserPageElement p where p.id.pageElementId in (:elementIds) and p.id.username=:username")
    List<UserPageElement> getByElementId(Collection<String> elementIds, String username);
}
