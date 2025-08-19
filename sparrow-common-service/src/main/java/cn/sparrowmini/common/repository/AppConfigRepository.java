package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.AppConfig;
import cn.sparrowmini.common.model.BaseState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends BaseStateRepository<AppConfig, String> {
}
