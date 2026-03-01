package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.ConfigItem;

import java.util.List;

public interface ConfigItemRepository<T extends ConfigItem> extends BaseRepository<T, String> {
    List<T> findByCatalogId(String catalogId);
}
