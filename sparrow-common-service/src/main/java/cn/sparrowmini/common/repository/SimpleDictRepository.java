package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.SimpleDict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

public interface SimpleDictRepository extends BaseRepository<SimpleDict, String> {

    @Query("select s from SimpleDict s where s.id=:id")
    Optional<SimpleDict> findById(String id);

    @Query("select s from SimpleDict s ")
    Page<SimpleDict> findAll(Pageable pageable, Specification<SimpleDict> specification);

    @Transactional
    @Modifying
    @Query("delete from SimpleDict s where s.id= :id ")
    void deleteById(String id);

    @Transactional
    @Modifying
    @Query("delete from SimpleDict s where s.id= :ids ")
    void deleteAllById(Collection<String> ids);
}
