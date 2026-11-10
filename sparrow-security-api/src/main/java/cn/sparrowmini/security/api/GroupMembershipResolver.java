package cn.sparrowmini.security.api;

import java.util.Set;
/** Returns current, trusted, namespace-qualified group facts; unavailable sources must fail closed. */
@FunctionalInterface
public interface GroupMembershipResolver { Set<String> groupsFor(String subject); }
