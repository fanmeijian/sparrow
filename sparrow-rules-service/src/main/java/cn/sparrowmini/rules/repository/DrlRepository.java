package cn.sparrowmini.rules.repository;

import cn.sparrowmini.common.repository.BaseRepository;
import cn.sparrowmini.common.repository.BaseStateRepository;
import cn.sparrowmini.rules.model.Drl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DrlRepository extends BaseStateRepository<Drl, String> {
    Optional<Drl> findByCode(String code);
}
