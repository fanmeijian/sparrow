package cn.sparrowmini.common.service;

import cn.sparrowmini.common.constant.StorageTypeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileStorageFactory {
    private final Map<StorageTypeEnum, StorageService> services = new HashMap<>();

    // Spring 会自动注入所有实现 FileStorageService 的 Bean
    public FileStorageFactory(List<StorageService> storageServices) {
        for (StorageService service : storageServices) {
            services.put(service.getStorageType(), service);
        }
    }

    public StorageService getService(StorageTypeEnum type) {
        return services.get(type);
    }
}
