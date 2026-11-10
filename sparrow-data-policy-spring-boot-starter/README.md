# 可选数据权限装配

依赖本 starter 启用数据权限，也可设置 `sparrow.data-policy.enabled=false` 关闭。它依赖 FunctionPermissionEvaluator 契约，不要求 RBAC 实现。

宿主必须提供 EntityManager、JpaPolicyStore.Guard、DataPolicyRegistration 和功能 evaluator。注册表只包含允许使用的业务资源、字段及每项功能所需的扩展名称；项目通过 DataPolicyExtension 实现额外范围限制。缺少注册的必需扩展、扩展名称重复或缺少功能 evaluator 时启动失败。命名空间由 `sparrow.data-policy.namespace` 设置，回退 identity namespace，再回退 default。

通过 DataPlanFactory 获取当前功能资格、已发布规则和策略 revision，再与业务扩展条件取交集；查询通过受控 reader 在分页和计数之前执行过滤。扩展只能进一步限制范围。不要将此工厂的结果缓存为长期主体权限。

自定义 evaluator 默认不声明角色资格证据；此时禁止发布 ROLE ALLOW，启动时也拒绝已有的此类有效策略。ROLE DENY 仍用于限制，USER/GROUP 规则不受该能力开关影响。角色证据不能简单使用用户所有角色替代。

src/test 提供无 RBAC 的函数授权、业务所有者限制、发布/撤销及缺失依赖的装配测试。
