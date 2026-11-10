package cn.sparrowmini.identity.api;

import java.util.*;
public interface UserDirectoryProvider {
    String code();
    UserProfile get(String id);
    List<UserProfile> list(String usernamePrefix, int offset, int limit);
}
