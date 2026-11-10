# Keycloak 角色适配

KeycloakRoleProvider 实现角色目录、成员分配和角色引用验证。每个实例绑定一个 realm 角色目录，或一个明确的 client UUID 对应的目录；client UUID 不等同于 clientId。

持久化功能授权使用 `role:` 加 issuer + Keycloak role UUID 的稳定标识，不以可修改的角色名称作为外键。rolesFor 每次请求有效角色（包含 composite），不缓存 JWT 角色快照；远端故障直接失败，不回退旧角色。角色成员查询使用角色名称路径，返回直接用户/群组分配，不声称列出全部间接继承成员。

管理操作由 RbacManagementGuard 控制，运行时角色事实与 RoleReferenceValidator 不要求目录管理权限。宿主不能把运行时内部方法暴露为未授权的目录查询 API。

可与 rbac-spring-boot-starter 的独立 JpaRoleGrantStore、AssignedRolePermissionEvaluator 组合。Keycloak 管角色目录与成员，Sparrow 管业务 resource/scope 授权及可选数据策略。

Keycloak 角色没有 enabled 开关，capabilities 只声明 QUERY、CREATE，setEnabled 明确拒绝。复合角色编辑尚未提供命令接口；已有复合关系可以用于有效角色解析。目录、成员扫描均有明确上限。
