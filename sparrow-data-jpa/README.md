# 通用 JPA 查询

本模块依赖 core 与 JPA/Spring Data，不引入 RBAC 或数据权限。项目在服务层完成功能授权，并提供自己的 Specification 数据约束。

- JpaResource：注册资源编码、实体、单一标量主键和公开字段，通过 Metamodel 校验继承属性。
- JpaPredicateCompiler：将 Condition 转为 Specification / Criteria Predicate，拒绝未知字段和类型不匹配。
- JpaQueryReader：每次调用要求提供非空的必需 Specification，用于分页前筛选和总数查询；返回不可变值映射。最大页大小 500，主键保证稳定排序。

```java
var resource = new JpaResource<>("order", Order.class, "id", Set.of("id", "owner", "title"));
var reader = new JpaQueryReader<>(entityManager, resource, () -> {
    String owner = trustedCurrentSubject(); // 从可信上下文取得并冻结本次主体
    return (root, query, cb) -> cb.equal(root.get("owner"), owner);
});
// 应用服务事务内，先完成功能授权，再查询。
var page = reader.read(new Condition.All(), PageRequest.of(0, 20), Set.of("id", "title"));
```

Specification 只提供谓词，不修改投影、分页或排序；涉及一对多关系时使用 EXISTS 子查询，避免连接放大计数。项目负责事务及一致性级别。

通用 reader 没有逐行字段策略，需要该能力时使用 data-policy-jpa 的 SecuredJpaReader。当前适配器拒绝复合主键和关联字段输出；复杂领域查询与关联写入由项目提供明确命令，没有任意实体类名 HTTP 路由或通用 upsert。
