package cn.sparrowmini.permission.app;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRepository extends JpaRepository<App, String> {
    Optional<App> findByCode(String appId);
}
