# Sparrow

Sparrow 是面向 Java 业务项目的可组合基础框架。3.0 重构将身份认证、功能授权、行/字段数据权限和系统管理分开：项目可以只使用 RBAC 并自行实现范围控制，也可安装数据权限模块后继续追加业务限制。

## 当前 3.0 模块

| 层次 | 模块与职责 |
| --- | --- |
| 通用基础 | core：类型化条件；data-jpa：字段注册与 Specification/Criteria 查询 |
| 安全契约 | security-api：主体、功能决策；security-spring：方法授权适配 |
| 身份 | identity-api：可替换接口；identity-local 及 starter：本地密码、登录锁定、会话失效 |
| 功能授权 | rbac-core/jpa 及 starter：角色、成员、资源/scope 授权，可替换角色目录 |
| 可选数据权限 | data-policy-core/jpa 及 starter：发布规则、行范围、逐行字段读写、追加约束 |
| 群组与层级 | group-core/jpa：嵌套群组；tree-jpa：独立 TREE/DAG 节点关系 |
| 文件 | storage-api/local、file-service：存储及引用；file-policy：可选数据权限 |
| 系统管理 | system：字典、页面元素、配置、导航、审计；web：可选管理 HTTP 接口 |
| 独立宿主 | admin-server：本地身份管理服务器与管理页面 |

数据访问优先使用 Specification 和 CriteriaBuilder。实体不承担权限判定，不需要继承权限基类；新实现没有旧 API 兼容转发层。业务引用检查及父对象权限由项目适配。

## 构建与验证

要求 Java 17、Maven。从仓库根目录执行：

```sh
mvn -f sparrow-platform/pom.xml package
python3 scripts/verify-v3-architecture.py
node --test sparrow-admin-server/test/ui-capabilities.test.mjs
```

依赖已缓存时 Maven 可添加 `-o`。3.0 的构建入口是 sparrow-platform。数据库、存储与首次管理员初始化见 [admin-server README](sparrow-admin-server/README.md)，没有默认管理员密码。

## 接入与实施状态

- [架构设计](docs/refactoring/architecture.md)
- [逐阶段实现与验证记录](docs/refactoring/implementation-status.md)
- [RBAC 独立装配](sparrow-rbac-spring-boot-starter/README.md)
- [可选数据权限装配](sparrow-data-policy-spring-boot-starter/README.md)
- [通用查询](sparrow-data-jpa/README.md)
- [原 common 分析](sparrow-common/README.md)与[原 common-service 分析](sparrow-common-service/README.md)

历史 common/common-service 及旧业务模块保留用于源码归属核对，未纳入 3.0 reactor；旧调用方尚未全部切换。历史数据库迁移按约定留待用户单独处理，Keycloak 实际适配器由后续项目实现。实施记录中的剩余项仍需继续完成，架构设计不等于全部已经交付。
