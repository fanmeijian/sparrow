package cn.sparrowmini.identity.api;

import java.util.Objects;
/** No credentials or role snapshots are included in public profile data. */
public record UserProfile(String provider, String namespace, String id, String username, String displayName,
                          boolean enabled, long version) {
    public UserProfile { Objects.requireNonNull(provider); Objects.requireNonNull(namespace); Objects.requireNonNull(id); }
}
