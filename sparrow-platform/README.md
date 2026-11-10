# Sparrow 3 授权核心

新版 Maven 聚合入口，Java 17。当前实现是重构的第一个纵向切片，不依赖 `sparrow-common` 或 `sparrow-common-service`，不提供旧接口兼容层。

## 已实现

- `sparrow-security-api`：可信身份快照、资源/scope、主体、功能授权结果和 evaluator 契约。
- `sparrow-rbac-core`：显式注册的资源/scope、角色允许/拒绝、拒绝优先、保留具备当前 scope 的角色来源。
- `sparrow-data-policy-core`：结构化条件、行规则、逐行字段读写规则，以及只会收窄的行约束组合。

身份/角色/群组 ID 由宿主提供带命名空间的稳定编码。规则为不可变快照，调用方负责加载最新版本并解析角色/群组继承。FunctionDecision 是服务端内部结果，不能接受客户端提供的对象作为授权凭证。

## 测试

从仓库根目录运行：

```bash
mvn -f sparrow-platform/pom.xml test
```

本地依赖齐备时可增加 `-o` 离线执行。测试使用 JUnit 5.10.3，与本机完整缓存对齐。

授权测试涵盖 81 种双角色功能许可/行许可组合及规则顺序反转，并覆盖不同区域记录的字段隔离、字段拒绝、只读/可写分离、用户/群组策略、未知 scope、无匹配规则、约束只收紧、显式 null 与缺失属性、非法授权依据。

## 当前边界

当前 DataAccessPlan 在可信内存记录上判断，不能用于“先分页再过滤”的数据库执行。条件目前支持 All、Equal、And；类型按 Java 值精确匹配，不隐式转换。不支持的值类型明确失败。

尚未实现：资源字段 schema 校验、更多条件/操作符、JPA Specification/Criteria 编译与受保护访问入口、数据策略持久化/发布版本、字段追加约束和脱敏、本地账号/角色数据库及 Spring Security 集成、群组管理和管理页面。已有旧模块未切换，不能将本切片视为已完成生产迁移。

完整目标见 [架构设计](../docs/refactoring/architecture.md) 和 [迁移清单](../docs/refactoring/migration-checklist.md)。
