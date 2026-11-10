package cn.sparrowmini.storage.api;

import java.util.*;
public final class StorageRegistry {
    private final Map<String,StorageProvider> providers;
    public StorageRegistry(Collection<StorageProvider> providers){
        var map=new HashMap<String,StorageProvider>();for(var p:providers)if(map.putIfAbsent(p.code(),p)!=null)throw new IllegalArgumentException("Duplicate storage provider");this.providers=Map.copyOf(map);
    }
    public StorageProvider require(String code){var p=providers.get(code);if(p==null)throw new IllegalArgumentException("Storage provider unavailable");return p;}
}
