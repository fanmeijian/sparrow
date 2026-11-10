# RBAC 装配

依赖本 starter 可启用角色、资源和 scope 功能授权，不要求安装数据权限模块。

默认 `sparrow.rbac.provider=local`，提供本地角色目录、成员分配和功能授权。宿主必须提供 EntityManager、RbacManagementGuard 与注册的 `Set<PermissionRequest>`；命名空间使用 `sparrow.rbac.namespace`，其次 identity namespace，默认 default。管理 guard 必须检查当前操作者的授权边界。

外部模式设置 provider 为实现的 code，并提供 RoleDirectoryProvider、RoleAssignmentProvider。若复用本地功能授权存储，还必须提供 RoleReferenceValidator 和 GrantManagementGuard；只扫描 grant 实体包，不创建本地角色目录。授权 guard 接收修改前后 effect，撤销 DENY 也必须检查。也可自行提供 RolePermissionManager 和 FunctionPermissionEvaluator 替换存储/决策。缺失选定提供方时启动失败。

AssignedRolePermissionEvaluator 每次读取当前成员角色，再组合注册的功能授权，不信任请求传入的旧角色集合。支持角色资格证据的 evaluator 必须显式声明 supportsRoleEvidence，并返回实际允许该功能的角色；数据权限据此绑定同一角色的行规则。

本模块不内置 Keycloak 客户端；可组合 [rbac-keycloak](../sparrow-rbac-keycloak/README.md) 和 [Keycloak starter](../sparrow-keycloak-spring-boot-starter/README.md)。参考 src/test 下的外部目录、委派授权和即时撤权场景。
