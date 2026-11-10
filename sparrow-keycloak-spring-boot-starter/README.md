# Keycloak 装配

设置 `sparrow.identity.provider=keycloak` 启用。默认只装配主体桥接与 JWT decoder，不创建本地账号表。宿主仍须提供 SecurityFilterChain，将 decoder 用于 OAuth2 Resource Server；已有 decoder 会被保留，由宿主负责其校验配置。

```yaml
sparrow:
  identity:
    provider: keycloak
  rbac:
    provider: keycloak
  keycloak:
    issuer: https://identity.example/realms/project
    server-url: https://identity.example
    realm: project
    namespace: project
    audience: backend-service
    role-clients: [backend-service]
    management-enabled: ${KEYCLOAK_MANAGEMENT_ENABLED:false}
    management-client-id: ${KEYCLOAK_MANAGEMENT_CLIENT_ID:}
    management-client-secret: ${KEYCLOAK_MANAGEMENT_CLIENT_SECRET:}
```

内置 decoder 校验签名、有效期、issuer 和 audience，缺少 audience 时拒绝装配。启用管理时还必须提供 IdentityManagementGuard；启用 Keycloak 角色目录时必须提供 RbacManagementGuard。认证 issuer 与管理 realm 必须一致，配置缺失不回退本地管理。

管理账号先在 Keycloak 对应 realm 创建启用 Client authentication 和 Service accounts roles 的专用 client，再在后端环境注入凭据。管理功能默认关闭；只验证 JWT 不需要管理密钥。不要把管理凭据放到前端或提交到 Git。角色目录默认使用 realm roles，client roles 可设置 role-client-uuid。

若同时安装本地身份 starter 的 provider registry，则需要启用管理并提供实际 UserDirectoryProvider；仅做 JWT 认证的宿主无需安装本地身份 starter。外部管理操作不受本地数据库事务回滚保护。
