# Sparrow 管理 HTTP 适配器

该模块把已安装的管理服务暴露为 JSON API。启用方式：

```properties
sparrow.management.enabled=true
```

默认不启用。应用显式引入需要的业务模块并声明其服务 Bean；没有服务的模块不会注册对应控制器。身份、角色、策略、群组、字典和文件依赖标记为 optional，不强制下游应用安装全部模块。角色控制器依赖 `RoleDirectoryProvider`、`RoleAssignmentProvider` 和 `RolePermissionManager`，可以使用项目自己的身份/角色提供方。

| 路径 | 服务与操作 |
| --- | --- |
| `/api/admin/users` | 用户列表、创建、资料、启停和密码重置；只读 provider 拒绝写入 |
| `/api/admin/roles` | 角色列表、创建、启停、成员与资源/scope 授权 |
| `/api/admin/policies` | 指定资源/scope 的草稿、发布和撤销 |
| `/api/admin/groups` | 群组、成员、嵌套、启停和删除；提交命名空间 revision |
| `/api/admin/dictionaries` | 字典及字典项管理 |
| `/api/admin/files` | 元数据、上传、下载、重命名和分阶段删除 |
| `/api/session` | 当前主体、已许可功能及可用的 CSRF token |

宿主必须配置认证过滤链、可信的 `CurrentActorProvider`、`FunctionPermissionEvaluator` 和注册的 `Set<PermissionRequest>`。服务的管理 guard 执行后端权限检查；控制器不授予额外权限。会话模式保留 Spring Security CSRF 保护，客户端应把 `/api/session` 返回的 token 放入指定请求头。无 CSRF token 时返回空对象以支持无会话宿主，这不改变宿主安全配置。

`/api/session` 的功能列表只用于页面呈现，不替代逐请求、逐记录的授权。密码不会出现在用户响应中。拒绝访问返回 403，版本冲突返回 409，非法 JSON 返回 400；响应不暴露内部异常文本。

当前交付为 HTTP 适配器，还没有完整管理 UI 或独立管理服务器。下游应显式配置数据源、实体扫描、事务、资源 schema 和业务引用检查。不要将测试中简化的提供方或 guard 作为生产配置。
