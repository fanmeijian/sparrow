package cn.sparrowmini.common.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Component;
import org.springframework.data.repository.core.RepositoryInformation;

import java.util.Optional;

@Component
public class RepositoryUtils {

    private final Repositories repositories;

    private static RepositoryUtils INSTANCE;

    @Autowired
    public RepositoryUtils(ApplicationContext applicationContext) {
        this.repositories = new Repositories(applicationContext);
    }

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public static Class<?> getDomainType(Object repository) {
        // 优先尝试直接查找
        Optional<RepositoryInformation> infoOpt =
                INSTANCE.repositories.getRepositoryInformationFor(repository.getClass());

        // 如果直接找不到，遍历所有 Bean 尝试匹配接口
        if (infoOpt.isEmpty()) {
            for (Object candidate : INSTANCE.repositories) {
                if (candidate.getClass().isAssignableFrom(repository.getClass())) {
                    return INSTANCE.repositories.getRepositoryInformationFor(candidate.getClass())
                            .map(RepositoryMetadata::getDomainType)
                            .orElseThrow(() -> new IllegalStateException("找不到实体类型"));
                }
            }
        }

        return infoOpt
                .map(RepositoryMetadata::getDomainType)
                .orElseThrow(() -> new IllegalStateException("无法解析实体类型"));
    }

    public static Class<?> resolveIdClassFromRepository(Object repository) {
        return INSTANCE.repositories.getRepositoryInformationFor(repository.getClass())
                .map(RepositoryInformation::getIdType)
                .orElseThrow(() -> new IllegalStateException("无法解析主键类型"));
    }
}