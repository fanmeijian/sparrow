# Keycloak 身份适配

独立于本地账号和 JPA。KeycloakActorProvider 消费宿主认证过滤链已验证的 JWT，检查配置 issuer，并用 issuer + sub 的 Base64URL 编码组合生成稳定主体 ID。用户名变化不会改变 ID；不接受匿名主体或任意自定义 roles 声明。

JWT 角色快照使用 `realm:角色名`、`client:Base64URL(clientId):角色名`，保留大小写，只读取配置的 client。它不承诺令牌有效期内的即时撤权；需要当前角色事实时使用 rbac-keycloak 的角色分配适配器，再由 AssignedRolePermissionEvaluator 决策。这两类角色标识不能混用。

KeycloakAdminClient 使用固定服务器/realm 和 client_credentials，缓存短期服务 token，管理错误不回传凭据或远端响应正文。生产宿主应使用超时明确的 HTTP 客户端；starter 配置连接超时 5 秒、读取超时 10 秒。失败写请求不会自动重试。

KeycloakUserProvider 提供查询、创建、资料修改、启停、密码重置和注销会话，所有管理入口先执行 IdentityManagementGuard。createDisabled 可用于先建停用账号、分配角色、最后启用的工作流。返回资料不含密码。

Keycloak 用户接口没有本地 JPA 乐观锁语义，返回 version=-1，更新必须显式提交 -1；不能将其解释为并发比较交换。资料更新仅发送本次变更字段。远端操作与宿主数据库事务不具备原子性，调用方必须记录部分成功并避免盲目重试。注销会话也不等于所有离线校验的 JWT 立即失效。

用户名前缀在 REST 子串搜索结果中进一步过滤，之后才计算分页；扫描上限 10000，超限报错而非返回不完整结果。

已通过模拟 HTTP 契约测试；尚未对具体部署的 Keycloak 版本完成真实管理联调。接口依据：[Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/index.html)。
