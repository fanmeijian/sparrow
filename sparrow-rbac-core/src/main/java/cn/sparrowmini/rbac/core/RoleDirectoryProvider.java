package cn.sparrowmini.rbac.core;

import java.util.List;
public interface RoleDirectoryProvider {
    default java.util.Set<RoleCapability> capabilities(){return java.util.Set.of(RoleCapability.values());}
    String code();
    List<RoleProfile> list(int offset,int limit);
    RoleProfile create(String code,String name);
    RoleProfile setEnabled(String id,long version,boolean enabled);
}
