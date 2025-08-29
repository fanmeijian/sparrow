package cn.sparrowmini.bpm.server.form;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormSchemaRepository extends JpaRepository<FormSchema, String> {
    Page<FormSchemaView> findBy(Pageable pageable);
    // 如果你使用的是 Spring Data 的动态投影（返回类型作为参数）
    <T> Optional<T> findById(String id, Class<T> type);
}
