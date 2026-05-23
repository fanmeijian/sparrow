package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.ViewConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewConfigRepository extends BaseRepository<ViewConfig,String> {

    Optional<ViewConfig> findByCode(String code);

    boolean existsByCode(String id);
}
