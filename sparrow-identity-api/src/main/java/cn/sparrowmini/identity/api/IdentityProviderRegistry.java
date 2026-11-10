package cn.sparrowmini.identity.api;

import java.util.*;
public final class IdentityProviderRegistry {
    private final Map<String,UserDirectoryProvider> providers;
    public IdentityProviderRegistry(Collection<UserDirectoryProvider> implementations) {
        var map=new HashMap<String,UserDirectoryProvider>();
        for(var provider:implementations) if(map.putIfAbsent(provider.code(),provider)!=null) throw new IllegalArgumentException("Duplicate identity provider");
        providers=Map.copyOf(map);
    }
    public UserDirectoryProvider select(String code) {
        var provider=providers.get(code);
        if(provider==null)throw new IllegalArgumentException("Identity provider is not installed: "+code);
        return provider;
    }
}
