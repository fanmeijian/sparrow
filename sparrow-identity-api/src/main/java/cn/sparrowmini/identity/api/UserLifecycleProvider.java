package cn.sparrowmini.identity.api;

public interface UserLifecycleProvider extends UserDirectoryProvider {
    UserProfile create(String username, String displayName, String password);
    UserProfile update(String id, long expectedVersion, String displayName);
    UserProfile setEnabled(String id, long expectedVersion, boolean enabled);
    void resetPassword(String id, long expectedVersion, String password);
}
