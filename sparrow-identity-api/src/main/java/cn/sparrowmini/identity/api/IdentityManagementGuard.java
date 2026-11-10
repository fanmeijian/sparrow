package cn.sparrowmini.identity.api;

/** Host-enforced management and namespace boundary. Never derive this from client role claims. */
@FunctionalInterface
public interface IdentityManagementGuard {
    void require(String namespace, String scope, String subjectId);
}
