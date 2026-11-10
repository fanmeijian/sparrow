package cn.sparrowmini.identity.api;

/** Provider-supported operations; caller permission is checked separately. */
public enum IdentityCapability { QUERY, CREATE, UPDATE, SET_ENABLED, RESET_PASSWORD }
