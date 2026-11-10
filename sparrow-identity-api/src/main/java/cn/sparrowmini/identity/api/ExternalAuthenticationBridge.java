package cn.sparrowmini.identity.api;

/** Explicit host integration for external authentication (issuer, subject mapping and filter-chain validation). */
@FunctionalInterface
public interface ExternalAuthenticationBridge { void validateConfiguration(); }
