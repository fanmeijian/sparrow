package cn.sparrowmini.bpm.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface ProjectionJpaRepository<S,ID>{
    <T> Page<T> findBy(Pageable pageable);
    // 如果你使用的是 Spring Data 的动态投影（返回类型作为参数）
    <T> Optional<T> findById(ID id, Class<T> type);
}
