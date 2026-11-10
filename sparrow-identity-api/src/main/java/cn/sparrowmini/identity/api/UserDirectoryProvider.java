package cn.sparrowmini.identity.api;

import java.util.*;
public interface UserDirectoryProvider {
    default java.util.Set<IdentityCapability> capabilities(){return java.util.Set.of(IdentityCapability.QUERY); }
    String code();
    UserProfile get(String id);
    List<UserProfile> list(String usernamePrefix, int offset, int limit);
}
