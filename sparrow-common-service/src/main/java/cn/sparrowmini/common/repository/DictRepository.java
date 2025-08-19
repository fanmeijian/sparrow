package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.Dict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DictRepository extends BaseTreeRepository<Dict, String> {
    Optional<Dict> findByCode(String code);
}
