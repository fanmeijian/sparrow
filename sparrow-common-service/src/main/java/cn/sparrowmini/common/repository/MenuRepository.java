package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface MenuRepository extends BaseTreeRepository<Menu,String>{

//    @Query("""
//            select m from Menu m
//            where (:parentId is null or m.parentId=:parentId)
//            and (m.id in (select um.id.menuId from UserMenu um where um.id.username=:username)
//            or m.id in (select sm.id.menuId from SysroleMenu sm where sm.id.sysroleId in (:roles)))
//            """)
//    Page<Menu> getUserMenu(String parentId, String username, Collection<String> roles, Pageable pageable);
}
