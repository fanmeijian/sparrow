# Sparrow 基础库重构设计

状态：架构决策基线，按本文执行；代码与数据库迁移尚未实施。依据：当前仓库源码静态检查。适用范围：`sparrow-common`、`sparrow-common-service` 及必须同步更新的调用方。迁移执行步骤见 [迁移清单](migration-checklist.md)。

## 1. 设计约束

1. 新版按破坏性升级设计，目标主版本为 3.0.0。
2. 新运行时不保留旧包、类名别名、弃用转发类、旧接口路由、旧字段读写、双写、兼容开关或旧 schema 回退逻辑。
3. 历史数据适配由独立迁移工具和数据库版本脚本完成；历史调用方通过升级代码和配置适配。
4. 迁移工具允许识别旧格式，但独立构建、独立执行，不被应用依赖。保留迁移源码和执行报告用于审计、重演，不等于在运行时保留遗留代码。
5. 一次切换一个完整应用部署单元的前后端、任务、消费者和数据库；旧应用不能连接新 schema。不同独立数据库可以分别迁移。
6. 数据完整性、权限语义和可恢复性优先于名称美化。历史字段不能因新模型暂时未使用就直接丢弃。

## 2. 当前事实与重构动因

| 已确认事实 | 来源 | 设计后果 |
| --- | --- | --- |
| 模型 ID 来源于实体完整类名 | `ModelService.synchronize()` | 权限资源标识与 Java 类名解耦 |
| 用户/角色模型授权查询按模型、动作和效果查询，未限定具体用户/角色 | `UserModel`、`SysroleModel` 的 `existByPermission` | 重写并验证主体匹配，不能把旧执行结果当作正确权限基线 |
| 允许配置检查仍传入 DENY | `ModelPermissionService` | 权限决策重新定义，不照搬旧分支 |
| scope 未配置时默认允许 | `ScopePermissionAspect` | 迁移时明确公开或授权策略，新版默认拒绝 |
| Controller 接收类名并调用 Class.forName | common-rest 的通用 CRUD/树 Controller | 改为稳定资源编码和受控注册表 |
| 前端包含实体和投影完整类名 | app-admin、rule-admin 中的常量和服务 | 前后端必须同步更新 |
| 字典以 entityType 判别；配置保存 valueType 并反射构造 | `SimpleDict`、`ConfigItem` | 类型编码及配置值需要数据迁移 |
| 两模块重复定义 TablePrefix | 两处 `model/TablePrefix.java` | 取消重复类，采用显式表映射 |
| 当前树 V1 为单父，V2 为多父关系 | `BaseTree`、`BaseTreeV2` | 不按新旧版本简单合并，显式区分树与有向无环图 |
| ViewConfig 使用 jsonb，其他配置存在 LOB/JSON Converter | `ViewConfig`、`AppConfig` | 真实数据库类型和存储形式必须盘点，不能假设所有 JSON 都是普通文本 |

本次未连接任何部署数据库；源码注解不能证明实际物理列名、约束、数据量或数据库版本。后续迁移需以 schema 导出和数据分析为准。

## 3. 目标模块与依赖边界

采用统一 Maven parent/BOM 管理版本、插件和测试约定。下表是目标职责，按可独立接入和测试的能力拆模块，避免只有转发代码的空层。已确认使用 security / rbac / data-policy 命名，原 sparrow-access 合并方案取消。

| 模块 | 根包 | 职责与允许依赖 |
| --- | --- | --- |
| sparrow-core | `cn.sparrowmini.core` | 极少量无框架公共值对象；没有实际复用需求时不创建 |
| sparrow-security-api | `cn.sparrowmini.security.api` | ActorContext、资源、scope、功能授权请求/结果、身份与组织事实接口；不依赖 Spring/JPA |
| sparrow-identity-api | `cn.sparrowmini.identity.api` | 用户目录/生命周期 SPI、能力声明、稳定用户 DTO；依赖 security-api，不依赖身份源 SDK/JPA |
| sparrow-identity-local | `cn.sparrowmini.identity.local` | 本次交付的本地用户/凭据 JPA 实现及 Spring Security 用户名密码认证适配；依赖 identity-api/security-spring/data-jpa |
| sparrow-identity-local-spring-boot-starter | 对应模块的装配包 | 本地身份默认装配，依赖本地 identity 实现，不强制引入数据策略 |
| sparrow-group-core | `cn.sparrowmini.group.core` | 可选本地群组、成员与嵌套计算、管理服务及存储端口；依赖 security-api，不依赖 rbac/data-policy |
| sparrow-group-jpa | `cn.sparrowmini.group.persistence.jpa` | 群组与关系持久化；依赖 group-core/JPA |
| sparrow-rbac-core | `cn.sparrowmini.rbac.core` | 角色—资源—scope 授权计算、角色目录/分配 SPI 和授权存储端口；只依赖 security-api |
| sparrow-rbac-jpa | `cn.sparrowmini.rbac.persistence.jpa` | 角色授权规则持久化及可选本地角色/分配实现；角色表按 provider 选择装配；依赖 rbac-core/JPA |
| sparrow-data-policy-core | `cn.sparrowmini.datapolicy.core` | 行/字段策略、结构化条件、约束合并、DataAccessPlan 和扩展 SPI；依赖 security-api，不依赖 rbac-core/JPA |
| sparrow-data-policy-jpa | `cn.sparrowmini.datapolicy.persistence.jpa` | 策略持久化、条件编译和 JPA 执行适配；依赖 data-policy-core/data-jpa |
| sparrow-security-spring | `cn.sparrowmini.security.spring` | 身份桥接、功能授权注解和 Spring 集成；依赖 security-api，不强制引入 RBAC/data-policy |
| sparrow-data-jpa | `cn.sparrowmini.data.jpa` | 审计、Repository、用户过滤、投影；不反向依赖 rbac/data-policy |
| sparrow-tree-jpa | `cn.sparrowmini.tree` | 层级、关系、排序与查询；依赖 data-jpa |
| sparrow-storage-api | `cn.sparrowmini.storage.api` | 对象存储 SPI、locator 和流契约；不依赖 JPA/Web |
| sparrow-file-service | `cn.sparrowmini.file` | 文件元数据、附件和业务操作；依赖 data-jpa/storage-api/security-api |
| sparrow-file-policy | `cn.sparrowmini.file.policy` | 可选文件行/字段执行适配；依赖 file-service/data-policy-jpa。基础文件服务不反向依赖数据策略 |
| sparrow-storage-local / sparrow-storage-txcos | `cn.sparrowmini.storage.local` / `.txcos` | 存储 SPI 实现，不反向依赖文件服务 |
| sparrow-system | `cn.sparrowmini.system` | 用户管理编排、字典、配置、导航与页面配置；各功能按需装配。字典为可选功能包，不独立拆 Maven 模块；使用 security-api，不强制依赖 RBAC/data-policy 实现 |
| sparrow-web | `cn.sparrowmini.web` | API、DTO、错误响应与受控动态入口；调用应用服务 |
| sparrow-rbac-spring-boot-starter | 对应模块的装配包 | RBAC 默认配置；不传递引入 data-policy 模块或表 |
| sparrow-data-policy-spring-boot-starter | 对应模块的装配包 | 数据权限配置；要求存在功能授权实现，不强制要求其来自 RBAC |

Spring 装配先放入对应集成模块，只有规模需要时才独立 autoconfigure artifact。不提供默认把所有业务模块全部引入的总 starter。实体、Repository 和数据库迁移均按启用能力选择，避免只用 RBAC 也必须创建数据策略表。

ActorContext 归 security-api，避免 core 和安全模块分别定义身份模型。组织/部门事实由宿主通过接口提供。群组事实通过 security-api 的 GroupMembershipResolver 接口获取，宿主选择本地 group 实现或外部身份源适配；不强制建立企业组织目录。RBAC 通过角色 provider 获取角色和分配事实，其本地默认实现保存角色及分配；不拥有群组成员表。

业务服务通过 security-api 获取功能授权，数据策略通过装配的执行适配器增强访问入口。仅用 RBAC 时，业务数据范围由宿主服务实现；启用数据策略时，业务扩展可继续收窄，不能覆盖拒绝。

功能内部按需要划分 model、application、persistence，例如 `system.dictionary.application.DictionaryService`。模块内部包不作为公共 API，使用构建约束验证依赖方向。

技术版本升级与职责重构分开验证。首轮保留 Java 17 基线，统一依赖管理后另行验证框架升级。

## 4. 核心模型与 API 决策

### 4.1 实体、DTO 与身份

- `AuditedEntity` 只包含创建/修改时间及主体 ID。实体主键、业务状态、启用标记由具体领域声明。
- 删除通用实体上的 `modelName`、`errMessages`、`children`、`expanded` 等展示职责，迁移到响应 DTO。
- `dataPermissionTokenId` 必须先追踪实际调用方与历史数据；有使用时迁为明确的数据访问策略关联，无使用证据时经数据清单确认后归档删除，禁止直接忽略。
- 核心实体新增 `@Version` 乐观锁，旧数据初始化版本；共享排序/层级变更另有事务内串行化策略。
- `CurrentActorProvider` 返回显式 ActorContext；身份来源由宿主提供。Web 适配器负责请求生命周期，后台任务使用显式 system actor。无身份不能等价于管理员。
- 不保留静态 `EntityManagerProvider` 或 Service Locator。审计回调只设置字段；树排序、权限查询等数据库操作进入应用服务事务。
- Jackson 实体注解不能代替输入 DTO 和字段写入规则。

### 4.2 稳定资源标识

建立 `ResourceRegistry`，每项使用稳定业务编码，例如 `system.menu`、`system.dictionary-entry`。编码来自显式声明，重命名 Java 类不改变编码。

资源描述包含可用动作、字段定义、查询能力、投影编码和服务处理器。注册冲突启动失败；未知资源或投影请求明确报错。注册资源不代表允许访问，仍必须完成授权。

外部请求不接受 Java 类名、任意 projectionClassName 或任意类加载。对确有动态管理需求的资源，使用 `resourceCode + viewCode`；核心业务提供明确命令 API。前端查询字段使用公开字段编码，内部映射到 JPA 属性。

`Model` → `ResourceDefinition`；`ModelAttribute` → `ResourceFieldDefinition`。稳定编码存储在权限及配置中，Java 类到资源的绑定只存在于新代码注册信息中。历史类名映射只存在离线迁移清单中。

### 4.3 查询与写入

统一查询路径：请求校验 → 功能授权 → 数据约束（按资源接入方式由框架或宿主提供）→ 受限查询 → 分页/投影结果。类型化布尔条件 Condition 由通用查询与权限判定复用，归属无框架的 sparrow-core；data-jpa 提供 JpaResource 注册表与 Specification/Criteria 编译，data-policy-core 负责授权组合，data-policy-jpa 提供受控行/字段执行。通用查询不依赖授权模块，data-policy-core 不依赖 data-jpa，也不提供任意代码表达式平台。

- 过滤字段白名单、类型检查、表达式深度/长度限制、完整输入消费和明确错误位置。
- 使用 JPA Metamodel 解析继承字段、嵌入字段和关联，不依赖 getDeclaredField 单层查找。
- 分页上限由接入配置确定；关联查询计数去重；投影仅返回授权字段。嵌套集合批量加载，测试查询次数和结果一致性。
- 权限条件与用户过滤使用 AND 合并，用户表达式不能覆盖权限条件。
- Repository 扩展保留在 `ExtendedJpaRepository` 与默认实现中；使用标准 Spring Data 能力即可完成的部分不重复实现。
- 写入分为 create/update/patch/delete。update/patch 不存在时返回明确错误，不因转换异常转为 insert。
- patch 明确缺省与显式 null 的差别；字段白名单、关联变更及版本冲突均有明确语义。
- upsert 只对有明确业务键和唯一约束的导入功能提供；并发冲突按该业务重试或失败，不暴露任意实体通用 upsert。
- 批量写入默认整批事务；确需部分成功时另设 API 和逐项结果，禁止静默吞错。

#### 数据访问实现规范（确定采用）

所有新实现及重构后的运行时数据库操作优先使用 Spring Data JPA 和标准 JPA API，尽量避免 native SQL。具体优先级如下：

1. 简单主键查询与实体增删改使用标准 Repository/EntityManager，简单固定查询可使用派生查询方法。
2. 动态条件、数据范围与组合过滤优先使用 `Specification<T>`；字段路径、AND/OR、关联和子查询通过 `CriteriaBuilder` 构建。
3. 复杂投影、聚合、分组、EXISTS、计数等使用 `CriteriaQuery`、`Subquery`、`Tuple` 或构造器投影；不为绕过 Specification 的接口限制直接改用 native SQL。
4. JPQL 仅在固定查询使用它能明显改善可读性时采用；动态授权条件仍应在 Criteria 路径统一组合，不能通过拼接查询文本实现权限表达式。
5. native SQL 仅作为例外：标准 API 无法合理表达所需能力，或经过执行计划与性能测量证明常规实现无法满足已确定目标时使用。开发便利本身不是引入 native SQL 的依据。

RBAC、群组、数据策略、系统业务和文件元数据访问都遵守本规范。用户过滤与权限条件分别构建，在同一查询的 Root/Join 上编译并按已定语义组合；不跨查询复用已构建的 Predicate/Path。分页数据查询与计数查询分别构造条件，确保范围一致并处理关联去重。

实体更新默认走受管实体和事务，保留审计、生命周期及乐观锁语义。批量操作确有必要时可采用 `CriteriaUpdate`/`CriteriaDelete`，但不能直接将依赖 CriteriaQuery 的任意 Specification 套用于批量 DML；需独立支持的条件编译路径，并明确授权检查、版本条件/更新、审计和持久化上下文同步。批量 DML 不因使用 Criteria 就自动获得实体回调或乐观锁保护。

树/群组递归先评估批量逐层查询或明确的关系索引设计，避免逐节点查询；如确需递归 CTE 等方言能力，按 native SQL 例外处理。不把 Criteria 当作必然高性能的保证，仍需检查 SQL、索引、查询次数和目标数据库执行计划。

native SQL 例外必须集中在 persistence 适配器内部，记录使用原因、已评估替代方案、支持方言及对应集成测试；所有值参数绑定，动态标识符仅从注册元数据白名单取得。调用方不接触 SQL，也不得绕过功能授权、行约束和字段约束。不默认引入 JdbcTemplate、手写 SQL Repository 或另一套 ORM 作为常规访问路径。

数据库 DDL、版本迁移、离线数据修复/转换及运维验证脚本不受运行时查询优先级限制，可使用经过审查的 SQL；它们独立于应用运行时，仍须满足迁移清单中的方言、数据完整性和恢复要求。

### 4.4 分层授权与可选数据策略

本节替代初版统一 PermissionGrant 方案。已确认的分层是：功能授权 → 可选数据权限 → 业务追加约束；各层之间只能收紧。

#### 功能授权

scope 固定表示资源上的操作，例如 `sales.order + read/update/approve`，不表示行范围。RBAC 建模为用户/组成员 → 角色 → resourceCode + scope。菜单可见性单独授权，不能推出业务操作许可。

`FunctionPermissionEvaluator` 接收可信 ActorContext 和权限请求，返回 ALLOW/DENY、原因码、规则/成员版本以及可用于追踪的授权依据。未知资源、scope、错误和无匹配许可都拒绝。匿名、系统任务和管理员必须显式赋权，不根据固定用户名绕过。

#### 三种接入方式

| 模式 | 功能权限 | 行/字段权限 | 业务控制 |
| --- | --- | --- | --- |
| 仅 RBAC | RBAC evaluator | 宿主服务负责 | 宿主服务 |
| RBAC + Data Policy | RBAC evaluator | 框架计划与执行适配器 | 仍可追加 |
| 自定义功能授权 + Data Policy | 自定义 evaluator | 同一数据策略接口 | 仍可追加 |

每个资源+scope 显式声明数据执行模式为 FRAMEWORK 或 APPLICATION_MANAGED。这是能力接入契约，不是历史兼容开关。FRAMEWORK 缺少策略允许、执行器或无法编译时拒绝；模块未安装却声明 FRAMEWORK 时启动失败。APPLICATION_MANAGED 由宿主实现数据约束，不等同于模块自动证明数据访问安全。

#### 数据模型

- RowPermissionPolicy：主体、资源、scope、ALLOW/DENY、行条件，可按属性或指定实例授权。
- FieldPermissionPolicy：主体、资源、scope、行条件、字段编码集合、读取/写入权限及可选脱敏规则。
- 实体只提供资源、主键和字段/关联映射；不强制继承权限基类或在每行保存权限 token。
- 指定行授权使用受控主键编码，支持复合键并包含资源及必要的租户命名空间，不能靠随意拼接字符串形成 ID。
- 功能授权通过才计算数据访问计划；字段授权不能扩大行范围。

#### 合并语义（确定采用）

同一策略域内允许范围取并集、匹配的拒绝范围排除；不同执行层之间取交集。无策略允许时为空集，不是全量。

```text
row(x) = functionAllowed
         AND systemBoundary(x)
         AND (OR rowAllow_i(x))
         AND NOT (OR rowDeny_j(x))
         AND customConstraint(x)

field(x, f, op) = row(x)
                  AND (OR fieldAllow_i(x, f, op))
                  AND NOT (OR fieldDeny_j(x, f, op))
                  AND customFieldConstraint(x, f, op)
```

行权限和字段权限均在同一 resource+scope 上求值。不同字段策略必须保留各自的行条件：不能把行条件先 OR、字段列表再合并，否则会把某角色对 A 区域成本字段的权限扩大到 B 区域。

角色组合保留授权来源：角色绑定的数据允许策略，仅在该角色同时拥有当前 resource+scope 功能许可时参与；用户级数据策略以功能授权整体通过为前提。拒绝规则对所有匹配的主体生效。功能 evaluator 必须为角色绑定的数据允许规则提供可信角色资格查询/依据；自定义 evaluator 无法提供而配置仍包含此类规则时，策略发布或启动校验失败，不能默认为全部角色有资格或静默忽略规则。角色继承资格包含明确的继承来源。

这能避免“角色 A 只有导出功能，角色 B 只有另一操作的数据范围，两者意外拼出导出全量数据”。新版不采用角色功能与角色数据范围独立拼接的语义，也不提供改变该合并规则的配置开关。

字段默认拒绝；确需全部字段时配置明确的允许字段集合，新字段默认不继承历史授权。脱敏不自动获得过滤/排序权限。多条脱敏规则无法比较强弱时拒绝该字段或使用资源预先定义的合并规则，不按注册顺序随意选择。

#### 扩展与执行

`DataConstraintContributor` 贡献额外结构化约束，不能贡献可覆盖拒绝的最终 boolean。返回值明确区分约束、拒绝、不追加；异常必须终止访问。已配置为必须存在的 contributor 缺失时启动失败。

`DataAccessPlan` 保存行条件、逐行字段规则、策略版本和待执行检查，由 JPA 执行适配器落实；计划不是仅靠持有即可绕过复核的访问凭证。复杂业务状态校验仍由应用服务执行，不强制塞入表达式语言。

- 列表、详情、计数、导出和批量写入使用同一授权入口；行过滤先于分页/计数。
- 不可读字段默认也不可用于用户筛选、排序、分组、聚合；框架内部为授权计算读取的字段不得序列化或写入业务日志。
- 关联对象不能因根对象可读而自动可读。首版要求注册关联的授权方式；未支持的嵌套关联拒绝查询，不退化为实体完整序列化。
- 不可见字段从 DTO 中省略，与真实 null 区分；可提供字段可读/可写提示供 UI 使用，但后端始终重新检查。
- create 对系统填入归属后的候选对象检查；update 同时检查旧记录、变更字段及新状态；删除按旧状态判断；部门/所有人转移增加独立 scope 或明确转移策略。
- 外部业务检查无法编译到 SQL 时，不得对分页结果临时剔除后声称分页正确；应提供可查询的事实映射、专用受限查询或明确不支持该列表能力。
- 原 Hibernate 权限监听器删除。普通 Repository、本地 SQL、数据库账号直连不是自动受保护边界；受保护资源由统一服务/执行器访问，依赖规则与集成测试检查绕过路径。

#### 生命周期、缓存和审计

策略支持 draft → validated → active → retired，发布前校验资源、scope、字段、类型和执行器能力，发布按版本原子切换，记录操作者及差异。权限管理本身使用独立管理 scope；普通角色管理员不能任意授予超出其委派范围的权限。

首版不缓存授权决策结果；后续增加结果缓存必须满足下面的约束。缓存键必须包含主体、角色/成员版本、资源、scope、策略版本及租户/组织上下文；不能只按用户和实体类型缓存。编译后的静态结构可以缓存，当前时间、记录状态等动态事实需要重新求值。

撤权生效边界确定为：新请求使用最新发布版本；已开始的事务使用一致版本；长时间导出/任务逐批重检。更强的即时撤权要求专门实现一致性协议，不承诺仅靠缓存失效即可实现。审计记录请求 ID、策略版本和原因，不记录敏感字段值；完整规则解释只对授权管理员开放。

### 4.5 层级模型

统一结构但保留语义差异：`Hierarchy`（mode=TREE/DAG）、`HierarchyNode`、`HierarchyEdge(parentNodeId, childNodeId, sortOrder)`。

- TREE 模式每个非根节点恰有一个父节点；DAG 允许多个父节点；两者禁止环、自引用、跨层级边。
- 根节点通过无入边表达；根排序保存在节点上，其他排序属于边。历史 null/空字符串等根标记通过清单转换。
- 关系设唯一约束 `(hierarchy_id,parent_id,child_id)`，父子外键和层级一致性必须验证。TREE 入边数量、环检测在同一层级锁保护的事务中执行。
- 业务对象通过节点绑定引用，避免所有业务实体都继承层级基类。内部可使用节点 ID 映射，业务对象原 ID 尽量保持。
- `detach` 仅删关系；`move` 原子变更关系和排序；`deleteNode` 默认拒绝存在依赖的节点。
- 子树删除作为单独命令：TREE 明确删除集合；DAG 有集合外入边时拒绝整批，要求先处理共享节点，禁止隐式递归误删。
- 并发移动以层级记录锁为初始实现，性能不足时再细化锁粒度。排序允许保留 decimal 精度，间距不足时事务内重排。

### 4.6 字典、配置和文件

#### 字典：可选的业务选项维护能力

字典保留在 `sparrow-system` 的 `system.dictionary` 功能包，不单独建立 Maven 模块，不作为安全框架首版的前置工作。未使用字典的应用不注册其服务、实体、Repository 或迁移脚本。字典可使用功能授权及可选数据策略；security、rbac、data-policy 均不依赖字典，资源和 scope 由权限模块定义。

字典仅承载需要运行时维护的分类、选项和编码，例如客户来源、行业分类。订单/审批状态归业务枚举或状态机，部门/项目/产品归独立业务实体，超时/上传限制归配置，固定协议编码归代码常量。新增选项需要新增业务行为时，不应只增加一条字典记录。

目标采用轻量模型：

| 模型 | 核心字段 | 约束 |
| --- | --- | --- |
| DictionaryDefinition | code、name | code 唯一且稳定 |
| DictionaryEntry | dictionaryCode、code、label、sortOrder、enabled | `(dictionaryCode, code)` 唯一；引用字典定义 |

- 不再通过 Java 实体继承、默认 discriminator 或实体扫描缓存表示字典类型。
- 默认使用平面条目；确有分级需求的字典才接入第 4.5 节的层级能力，不让普通字典强制依赖完整树模型。
- 被引用的条目编码保持稳定；名称可修改。停用仅禁止新增选择，历史记录仍能解析显示。
- 删除前验证业务引用，存在引用时拒绝删除；引用检查通过外键或登记的业务引用检查器落实。不能完整验证引用的字典仅允许停用，不提供物理删除。
- 业务需要保存当时名称时，由业务记录保存显示快照；不要求全部业务都采用快照。
- 历史子类的额外字段逐一迁入明确模型或定义过 schema 的扩展属性；存在独立行为和生命周期的类型迁为业务实体，不能只复制基类字段后丢弃其余数据。
- 已使用旧字典的应用，切换前必须完成该功能及数据迁移。字典可选不代表可以跳过既有数据；未使用字典的项目不因安全框架升级而引入字典。

#### 配置和文件

- 配置使用受控 valueKind（string/integer/decimal/boolean/json 等）及类型化值，未知自定义类型阻断迁移；不再依据数据库内容 Class.forName。
- 枚举和判别字段使用显式持久化编码，不依赖类名、枚举 ordinal 或默认 discriminator。
- `StorageProvider` 只处理对象流和 locator；`FileService` 处理 FileMetadata、附件和授权。registry 按 providerCode 注册，重复或缺失实现明确失败。
- 数据库事务不能与对象存储视为原子事务：上传采用待确认状态与失败清理；删除采用待删除记录与可重试任务。删除重试必须幂等。
- 迁移保留文件 ID 与实际对象定位；不要仅凭 path/url 推断存储位置。对象存储物理搬迁作为独立任务，有清单和校验。

### 4.7 页面元素与交互权限

页面元素权限是可选的 UI 能力投影，放在 `sparrow-system` 的 `system.presentation` 功能包，接口由 sparrow-web 暴露。它依赖 security-api 的功能决策接口，按资源接入方式组合 Data Policy 或宿主能力检查器；不强制依赖 RBAC 实现、Data Policy 模块或字典。前端通过独立的类型契约与指令/组件适配器接入，不将 DOM/框架组件类型写入后端核心模型。

当前 PageElementService 仅返回 elementId→ALLOW/DENY，不能明确表达隐藏、禁用、只读和记录上下文；新版改为元素定义、可选呈现策略和上下文决策三部分。

#### 稳定元素定义

`UiElementDefinition` 保存 pageCode、elementCode、kind、能力绑定、无权限呈现方式。elementCode 是稳定语义名称，如 `sales.order.detail.approve`，不使用 CSS 选择器、DOM 路径、Java 类名或框架组件实例 ID。

能力绑定分开声明：

- visibilityRequirement：该功能入口/分区是否可以被发现，按需要绑定资源+scope；可显式公开。
- interactionRequirement：执行操作需要的 resource+scope，以及可选字段/记录上下文。
- 字段元素必须以字段可读权限约束可见性，以字段可写权限约束编辑能力。
- 一个业务动作可绑定多个页面元素；元素并不自动生成一个新的业务 scope。仅确有独立发现/查看需求的分区定义独立资源或 scope。
- allOf/anyOf 只用于明确组合功能能力；各层权限交集、逐行字段条件和拒绝优先规则仍然成立。

#### 可选页面呈现策略

默认 DERIVED 模式从绑定的功能、数据及业务能力计算状态，无需为每个按钮重复维护用户/角色授权。需要单独控制不同用户/角色的展示时启用 RESTRICTED 模式，通过 `UiElementPolicy` 配置元素 view/interact 的 ALLOW/DENY。RESTRICTED 中无匹配允许则拒绝，匹配拒绝优先；未知元素、缺少必需定义或能力检查器校验失败。

呈现策略的 view/interact 许可仅在元素定义已经允许的能力范围内生效，不能放开数据字段或业务操作。它只控制页面体验；如果要求从任何入口禁止该动作，应配置 RBAC/Data Policy/业务规则，不能只隐藏按钮。父容器限制与子元素限制按交集组合，子元素不能恢复被父容器隐藏/禁用的交互。

#### 决策模型

输出 `UiElementDecision`：visible、enabled、readOnly、可公开 reasonCode、策略版本和可选记录版本。不要把 SHOW/HIDE/DISABLE 同时当成互相矛盾的数据库授权标志。

| 情况 | 输出与行为 |
| --- | --- |
| 不允许发现元素、字段不可读、父容器隐藏 | visible=false；不挂载受保护内容，不提供隐藏字段值 |
| 可见但无操作权限或业务状态不允许 | enabled=false；可按元素定义将此状态呈现为隐藏 |
| 字段可读但不可写 | visible=true、readOnly=true；控件不支持只读时以禁用/文本视图呈现 |
| 可见且具备操作权限 | enabled=true；前端本地校验、提交中状态仍可进一步禁用 |
| 加载中、计算失败、上下文不完整 | 不启用受保护交互；隐藏敏感内容或展示占位，不先全量显示再收起 |

无权访问记录时，不返回其状态、字段值或精细拒绝理由。UI 仅展示安全原因码，完整授权解释留给授权管理员。

#### 上下文和查询接口

后端提供页面级批量能力查询和记录级批量能力查询。请求包含注册的 pageCode/elementCode、resourceCode 和记录 ID，身份/角色来自服务端认证；不得信任前端传入的角色、归属、记录状态或“已授权”结果。候选新增/修改值按正式命令同样校验，能力预览不执行写入。

列表行按钮使用每行的决策，不能以一个页面级 boolean 控制全部行；批量操作默认要求所选记录全部满足条件，混合选择返回不可执行，不静默跳过无权项。批量查询设置大小上限并复用策略、组织事实及数据读取，避免逐按钮/逐行访问数据库。

预览与正式操作复用同一无副作用能力判断，但预览只是快照；实际 API 始终重新鉴权和验证事务状态。撤权、记录版本变化或执行被拒绝后，前端刷新能力，不能把 enabled=true 当作长期凭证。

前端统一适配 visible/enabled/readOnly；禁用同时阻断点击、键盘、快捷键及自定义组件事件，不只改变 CSS。原生 disabled 字段可能不提交，表单命令必须区分未提交与清空，禁止把隐藏/禁用字段默认补 null。字段脱敏由数据输出层执行，前端不接收原始敏感值后自行遮盖。

能力缓存必须绑定用户/租户、页面、记录上下文及策略/记录版本，登出或切换身份时清除；首版沿用不缓存授权结果的策略。页面策略与权限策略一样具有版本和发布校验，不向浏览器下发可执行策略脚本。

### 4.8 群组管理

群组表示“哪些主体属于同一个集合”，角色表示“具有什么功能权限”，部门/项目表示业务组织。三者分离。群组不自动等同于部门，不根据组名推断授权，也不将角色作为群组成员。

当前模型将 UserGroup、SysroleGroup 及 GroupUser、GroupSysrole、GroupRelation 放入继承表；GroupService 的成员分页/展开仍为占位实现。新版区分用户群组、角色分配和纯角色集合，不沿用混合 businessId 关系。

#### 模块与接入

本地群组是可选能力：sparrow-group-core/jpa 提供实现，管理 API 可由 sparrow-web 按条件暴露；RBAC 和 Data Policy 只依赖 security-api 的主体/群组事实契约。已有 LDAP、IdP 或业务组织服务的项目通过适配器提供有效群组，不需要本地群组表。

`GroupMembershipResolver` 返回当前主体的有效群组、事实版本和来源；不得依赖功能授权计算结果来解析成员，避免“解析群组→鉴权→解析群组”的递归。本地群组管理命令使用 FunctionPermissionEvaluator 授权；读取可信成员事实的内部端口与对用户开放的成员查询 API 分开。

群组身份使用 source+namespace+groupId，成员使用身份源下的稳定主体 ID，显示名称和用户名不是关系主键。source 表示权威来源，同名或相同外部 ID 不跨来源合并。首版通过单一聚合 resolver 接入各来源；外部同步群组只由其权威源维护，本地不得覆盖其成员关系；来源不可用时需要这些事实的授权失败，不能静默按空成员集合计算，尤其不能漏掉群组拒绝规则。

#### 本地模型

| 类型 | 核心内容 | 所属模块 |
| --- | --- | --- |
| Group | id、code、name、description、namespace、status、version | group |
| GroupMembership | groupId、subjectId、有效期、version；唯一成员关系 | group |
| GroupNesting | parentGroupId、childGroupId；唯一边、禁止自环/环 | group |
| GroupManagerAssignment | groupId、管理员主体、可管理能力/委派范围 | group |
| RoleAssignment | subjectType=USER/GROUP、subjectId、roleId、有效期 | rbac |

群组管理员身份不自动意味着业务成员身份。群组名称/描述可变，编码稳定；GroupManagerAssignment 不自动授予业务角色。首版成员只支持用户及服务主体，嵌套关系单独保存；不支持用任意 Java 类或 businessId 表示成员类型。

纯粹收集多个角色的“角色集合”不是群组。首版不引入角色集合的独立运行时权限机制；历史角色集合由迁移工具显式展开为角色分配或归档管理分类，不将其误转换为用户成员。

#### 嵌套语义

定义 parent 包含 child：child 的成员属于 parent 的有效成员，parent 的直接成员不会自动属于 child。例：华东销售组包含上海销售组，上海用户可获得授予华东销售组的角色；华东直接成员不会因此获得只授予上海组的角色。

首版支持显式嵌套 DAG，不自动根据业务部门树推导。所有关系在同一 namespace 内，禁止跨租户/命名空间嵌套；跨来源嵌套首版不开放。查询去重，保留来源路径供管理员审计；停用群组使经过该群组的成员推导路径失效，其他有效路径仍成立。根由没有父边推导，不保存可与关系矛盾的 isRoot 标志。

嵌套新增在 namespace 级锁保护的事务内检查环与配置深度上限，避免并发分别通过检查后形成环；超过深度/容量约束则拒绝，不能截断计算后继续授权。群组图与业务 Hierarchy 不共享表或实体；可复用纯算法，但安全身份解析不依赖业务树模型。

#### 授权接入

用户直接角色与通过有效群组获得的角色合并，保留角色及成员来源；继续遵守第 4.4 节的 scope 来源资格和拒绝优先。群组绑定的数据策略以其有效成员为主体，功能授权仍是前提；字段策略保留原行条件。群组本身不是新的业务资源 scope，它只是授权对象中的主体类型。

新增成员、删除成员、修改嵌套、群组停用或外部成员变更都可能改变授权，必须提升事实版本并失效受影响的祖先群组解析结果。首版不缓存最终授权结果，长任务按既定规则逐批复核；外部来源要明确同步时延，不能将“最新本地镜像”宣称为外部实时事实。用于高敏感拒绝/撤权的事实超出宿主规定的新鲜度时拒绝请求。

#### 管理边界与操作

管理 scope 分为 read、create、update、manage-members、manage-nesting、assign-managers、disable、delete；角色授予使用 RBAC 的 assign-role/revoke-role。群组管理员不能因管理群组而任意绑定角色。

仅隔离角色授予仍不足：向已绑定高权限角色的组添加自己也会提升权限。成员/嵌套变更必须校验该组及所有受影响祖先的委派边界，包含功能角色、数据策略和页面策略带来的权限变化。group-core 通过管理变更检查端口调用已装配的授权实现，不直接依赖它；存在权限绑定但缺少所需检查器时拒绝变更。成员自助加入与邀请流程不纳入首版。

移除成员、停用组也可能移除 DENY 规则而扩大访问，因此所有影响成员解析的操作都执行影响与委派校验，不能将删除一律认定为收紧。建立成员、关系与权限绑定引用查询接口，记录修改前后关系、操作者、原因和批次。

常用命令为 createGroup/updateGroup/addMembers/removeMembers/addChildGroup/removeChildGroup/disableGroup/deleteGroup，避免通用 Map upsert。批量关系操作幂等、默认整批事务；并发修改执行版本检查。删除存在成员、嵌套、角色/策略或业务引用的群组时拒绝，先显式解除关系；保留审计，不隐式级联删除授权。

动态规则群组、嵌套角色集合和自动组织同步平台不纳入首版；外部系统可以提供解析好的群组事实。

### 4.9 内置功能的管理服务与管理端

功能引擎、数据模型和管理功能分别验收。对宿主启用的内置功能，交付应用服务、管理 API、管理页面、管理权限及审计。用户管理本轮交付可扩展框架及可配置的本地数据库用户名密码实现，并验收真实本地流程；Keycloak 等外部实现由项目后续扩展。仅有接口、Repository 或实体不算完成已纳入本轮的管理功能。所有管理功能可按需接入，安全基础库不依赖管理端。

管理命令属于各功能的 application 层，Web 适配在 sparrow-web 按已启用能力装配。前端以现有 sparrow-app-admin 为管理应用承载，按功能提供独立路由和组件；页面通过能力发现接口展示已启用功能，管理应用不是 RBAC/Data Policy 的必需依赖。功能未安装时不注册其 API、路由或迁移脚本。API 可由宿主自建 UI 调用，避免管理规则只存在前端。

#### 管理功能交付矩阵

以下项目全部纳入计划；“可选”表示宿主可不启用该功能。用户管理框架和本地默认实现均在本轮交付，外部 provider 后续交付；宿主通过配置选择实现。

| 功能 | 管理服务/API 范围 | 管理页面与验收范围 | 主要归属 |
| --- | --- | --- | --- |
| 用户管理与本地默认实现 | 用户目录/生命周期 SPI、本地查询/创建/修改/启停/密码操作及可配置认证；角色/群组调用各自 provider | provider 能力驱动页面，真实本地数据库与登录/退出/管理流程验收；外部身份源另行接入 | identity-api/local、system.identity 管理编排及后续外部适配器 |
| 角色与功能授权 | 角色生命周期、用户/群组角色分配及撤销、资源+scope 授权、有效权限解释 | 角色管理、主体授权、资源/scope 选择、授权差异及来源 | rbac |
| 资源目录 | 注册资源/scope/字段/投影的查询、声明校验和部署同步 | 资源目录及能力详情、缺失绑定提示 | security 契约与各功能注册器 |
| 行/字段授权 | 策略创建/修改、结构化条件编辑、校验、预览、版本发布、停用/撤销 | 行范围表单、字段读写/脱敏表单、指定记录授权、版本差异/决策预览 | data-policy（可选） |
| 群组 | 群组及成员/嵌套管理、管理员分配、有效成员和授权影响查询 | 群组列表/详情、成员和父子关系、来源及影响展示 | group（可选） |
| 文件 | 上传、列表/元数据、受控预览/下载、重命名、附件关系、删除请求和失败重试 | 文件列表/详情、上传结果、业务引用、删除状态与失败原因 | file-service + storage provider |
| 文件权限 | 文件及指定文件授权查询、授予/撤销、到期控制、有效权限和来源解释 | 文件权限页：主体、动作、有效期、业务继承和直接授权说明 | file-service 编排 + data-policy 或宿主文件授权适配 |
| 字典 | 字典定义/条目管理、排序、启停、引用检查和删除限制 | 字典列表、条目编辑/排序、停用和引用提示 | system.dictionary（可选） |
| 配置 | 配置读取、类型校验、修改、发布/生效结果和附件管理 | 配置表单、值类型及变更历史；秘密值单独保护 | system.configuration（可选） |
| 菜单与页面元素 | 导航配置、元素注册和能力绑定、呈现规则校验/发布 | 菜单管理、元素及绑定、DERIVED/RESTRICTED 策略和状态预览 | system.navigation/presentation（可选） |
| 操作审计 | 管理变更记录、操作者、时间、目标、版本及结果查询 | 审计列表与受控详情；不记录密码或敏感字段原值 | 公共审计记录端口与管理集成 |

数据策略首版提供受控字段/操作符的表单编辑，不要求任意脚本或可视化规则编排平台。资源目录不能由管理员任意创建未实现的 scope 或绑定 Java 类，新增真实能力通过代码声明和部署同步。

#### 用户/角色管理框架、本地实现与外部扩展

本轮交付用户管理框架、可选本地数据库用户名密码实现和本地角色管理默认实现。项目创建时选择本地方案或自行提供外部方案；Keycloak 的真实用户/角色管理实现仍在后续单独提供。基础 SPI 不包含厂商类型，未选择的实现不参与 Bean、实体、端点或数据库迁移装配。

用户管理 SPI 归 sparrow-identity-api：UserDirectoryProvider（查询）、UserLifecycleProvider（可选写入）、ProviderCapabilities、稳定身份引用及 DTO/错误。system.identity 负责管理编排，不拥有账号数据库；本地实现放在 sparrow-identity-local，外部适配器独立依赖 identity-api。这样外部实现无需依赖整个 system 的业务代码。

角色目录与角色分配分别通过 rbac-core 的 RoleDirectoryProvider、RoleAssignmentProvider 接入；sparrow-rbac-jpa 提供本地默认实现。有效角色结果必须带来源、命名空间、稳定角色 ID 和成员版本/新鲜度，满足角色数据权限的来源资格规则。角色 provider 同时声明查询/管理能力，支持外部只读或可写实现，管理页面按能力开放。

认证、用户管理、角色管理是三个扩展点，通过同一身份引用连接。选择 Keycloak 用户管理不自动启用外部登录，也不自动将其 realm/client roles 当作 Sparrow 的 resource+scope 授权。外部模式由项目提供认证到 ActorContext 的桥接、用户管理 provider 和所选角色 provider；角色到资源/scope 的业务授权默认仍由 Sparrow RBAC 管理。需要替换整个功能授权引擎时另行实现 FunctionPermissionEvaluator，不与角色目录适配混为一谈。

#### 本地默认实现

本地账号包含稳定 ID、namespace、规范化登录名、显示资料、enabled/locked 状态、credentialVersion 和实体 version；凭据哈希与普通用户 DTO 分离。登录名唯一约束为 namespace+规范化登录名，改名不改变用户 ID。普通资料更新不能修改密码哈希、角色分配或账户安全状态。

认证沿用 Spring Security：SecurityFilterChain → AuthenticationManager/DaoAuthenticationProvider → 自定义 UserDetailsService → JPA 用户查询，密码校验由 PasswordEncoder 完成。Spring Security 的这条扩展路径使用 UserDetailsService 和 PasswordEncoder 验证用户名密码，参见 [官方文档](https://docs.spring.io/spring-security/reference/6.5/servlet/authentication/passwords/dao-authentication-provider.html)。不使用 JDBC 默认表结构或手写认证 SQL；本地查询继续遵守 Specification/Criteria 优先规范。

- 默认采用 DelegatingPasswordEncoder 的带编码标识哈希，当前编码器用 BCrypt，成本参数配置并实测；不保存明文或可逆密码。密码只通过专用创建/修改/重置命令输入，不出现在查询响应与审计中。
- 本地首版采用 Spring Security 会话登录/退出，保留 CSRF、防会话固定及生产 cookie 安全配置；不自建 JWT 签发或 OAuth 授权服务器。框架允许宿主替换认证集成，不在 external 模式注册本地登录链。
- 本地创建、资料更新、启停、密码修改、管理员重置、角色分配与管理 API/页面均真实实现。用户名不存在与密码错误使用一致的外部认证失败信息；登录失败限制、临时锁定和密码要求配置化并提供可用默认值，具体阈值纳入实施测试。
- 密码重置和停用提升 credentialVersion；后续请求校验账号状态/凭据版本，使旧本地会话不能继续获得访问。角色变更每次授权读取当前事实，不长期信任会话中旧角色集合。跨节点部署必须共享此校验所需事实，测试在途事务边界。
- 初始化管理员沿用受控部署引导，显式输入初始凭据并仅存哈希，无固定默认密码。用户删除先检查本地授权与业务引用；存在引用时拒绝删除并可停用，不级联删除业务数据。

#### 配置选择与装配

以下是新版本确定的配置契约示例，尚未在当前代码实现：

```yaml
sparrow:
  identity:
    provider: local
    authentication: local
  rbac:
    role-provider: local
```

自行提供外部实现的项目示例：

```yaml
sparrow:
  identity:
    provider: keycloak
    authentication: external
  rbac:
    role-provider: keycloak
```

provider 是注册编码，不内置“只写 keycloak 就可使用”的假实现。external 需要宿主显式提供认证集成。可选择外部用户+本地角色，但每个身份/角色命名空间只能有一个权威写入方，不自动双写；切换 provider 必须完成用户、角色、授权引用及会话迁移，不能当作无迁移热切换。

只有引入本地 starter 且没有显式选择其他 provider 时采用 local 默认值；基础 security/rbac 库不会悄悄启用本地账号系统。显式配置未知 provider、缺少对应依赖/实现或重复注册时启动失败，禁止回退本地。外部角色模式只装配所需的 Sparrow 业务授权表，不创建本地角色目录/分配表；外部用户模式不创建本地用户/凭据表。

#### 扩展契约与后续 Keycloak 实现

provider 声明查询字段/操作符、分页方式、可编辑字段及 QUERY/CREATE/UPDATE/ENABLE/DELETE/RESET_CREDENTIAL/SYNC 等能力；不支持的操作明确拒绝，页面不得假成功。厂商属性通过声明的扩展 schema/项目表单处理，公共接口不泄漏厂商 DTO。

用户和角色引用包含 provider/命名空间及稳定 ID，用户名和角色显示名都不作为跨系统主键。群组来源通过 GroupMembershipResolver 另行适配，不因选择 Keycloak 自动覆盖本地群组。外部身份/对象与本地授权数据库之间不建伪外键，不假设跨系统原子提交。

框架与本地实现本轮均需测试：SPI 路由、能力/鉴权、真实数据库账号生命周期、登录/退出、哈希校验、停用/重置撤权、本地角色及资源授权、装配互斥和迁移。外部可替换性通过测试 provider 验证；真实 Keycloak 后续验收 realm/角色映射、认证桥接、管理 API、会话时限和失败恢复。

后续 Keycloak 适配器通过其正式管理 API 实现用户/角色 SPI，不直接访问 Keycloak 数据库。基础框架不依赖其 SDK；Specification/Criteria 优先规范适用于自己的数据库操作，不限制调用身份提供方 API。

#### 文件权限管理

文件资源稳定编码为 `file.object`，scope 明确区分 read-metadata、upload、preview、download、update-metadata、delete、grant、revoke。存储 provider 负责对象访问，不决定业务授权。管理 API、直接文件下载、预览、异步导出和链接签发均通过 FileService 的授权入口。

文件权限采用既定分层：RBAC 决定功能资格；Data Policy 或 APPLICATION_MANAGED 宿主适配器决定可操作的文件及元数据字段；FileService 检查文件状态、业务引用及业务限制。文件 owner 不自动拥有无限授权能力，grant/revoke 除功能 scope 外还受委派范围约束。

FRAMEWORK 模式的指定文件授权写入同一套数据策略（资源+文件 ID+主体+scope+有效期），不另外建立与 Data Policy 竞争的 file ACL 引擎。APPLICATION_MANAGED 模式通过宿主文件授权管理端口完成授权查询/授予/撤销；启用了文件授权管理却未提供实现时启动失败，不能只展示空页面。

业务附件默认必须同时满足父业务对象访问约束和文件访问约束，直接文件授权不能放开父对象限制。多业务引用必须带明确业务上下文并验证真实绑定关系，不通过任意一个可见引用推导所有上下文可见。共享只是对已具备功能资格主体增加文件范围，不隐式创建 RBAC 角色或放开父对象权限；匿名公共分享不纳入首版。

首版受保护文件走服务端鉴权下载/预览，不持久化公开 URL。后续如引入预签名直链，必须明确 TTL 内无法依靠应用撤权立即撤销已发链接的边界；需要即时撤权的内容继续使用鉴权代理。权限页不暴露存储密钥、内部路径或未授权文件的详细存在信息。

#### 管理权限与引导

管理接口本身也使用资源+scope，读取、修改、授权、发布和导出分别控制；受托管理员只能管理指定命名空间/资源/主体范围。无 Data Policy 模块的管理端仍必须由应用服务执行管理范围约束，不能因为数据权限可选而开放全部管理对象。

授权变更使用明确命令和版本检查，记录前后差异、操作者、原因与结果；策略预览无副作用，不绕过正式授权。外部身份/对象存储操作不能假定与数据库原子提交，记录请求与结果，设计幂等及失败重试/补偿，并向管理页面呈现实际状态。

首次管理员通过部署引导命令绑定可信主体并创建显式管理授权，记录审计，执行后关闭引导入口；无公开 bootstrap 接口、无固定用户名后门。后续管理走正常授权流程；管理员锁定恢复采用受控运维流程，不在请求链路保留绕过逻辑。

完成标准：用户管理框架、本地账号认证/管理和本地角色实现通过真实数据库与集成测试，外部身份源由项目另行验收；其他宿主启用的内置功能均能通过页面和 API 完成其允许的管理流程；接口直调、越权主体、并发版本、撤权生效和失败重试均测试通过。未启用功能不影响基础库独立运行。

## 5. 主要类型迁移表

以下是目标设计映射，不是机械重命名脚本。拆分项必须按职责重写；完整文件级映射在实施阶段通过源码清单补齐。

| 旧类型/家族 | 新位置与名称 | 处理 |
| --- | --- | --- |
| CurrentUser / UserInfo | security.api.ActorContext / CurrentActorProvider | 替换静态上下文契约 |
| BaseOpLog / BasOpLogListener | data.jpa.audit.AuditedEntity / EntityAuditListener | 薄审计基类 |
| BaseState / BaseEntity / BaseUuidEntity / BaseData* | 领域实体及显式状态模型 | 拆除深继承，不建立同名替代层 |
| CommonJpaService / Impl、JpaService、BaseJpaService、InheritTableJpaService | query.EntityQueryService 与各领域 command 服务 | 收敛重复入口，删除任意 Map upsert |
| BaseRepository / Impl | data.jpa.repository.ExtendedJpaRepository / DefaultExtendedJpaRepository | 保留必要扩展 |
| CustomRepositoryFactory / Bean | data.jpa.repository 内部装配类型 | 仅按实际投影需要保留，非公共 API |
| PredicateBuilder / ExprToPredicateVisitor / 多套 JpaFilter | data.jpa.query.FilterParser / FilterExpression / JpaPredicateCompiler | 单一 AST 与编译路径 |
| ProjectionHelper / DynamicProjectionHelper / 其他辅助类 | data.jpa.projection.ProjectionQueryExecutor | 统一元数据和执行逻辑 |
| BaseTree / BaseTreeV2 / Tree DTO / Tree Repository / Service | tree 的 Hierarchy/Node/Edge、HierarchyService、HierarchyNodeView | 迁移数据后删除旧模型 |
| Model / ModelAttribute / ModelService | security-api 的资源契约与 data-policy 的字段元数据 | 类名改稳定编码 |
| Scope / ScopePermission / Aspect / Service | security-api 的 scope、security-spring 的 RequiresPermission / FunctionPermissionEvaluator | scope 映射到 resource+action |
| UserModel、SysroleModel、*Scope、*Menu、*ModelAttribute | rbac 的功能授权与 data-policy 的 Row/FieldPermissionPolicy | 按语义转换授权，保留来源追踪 |
| Sysrole / UserSysrole / SysroleRelation、pem.group.* | rbac 的 Role / RoleAssignment / RoleRelation；group 的 Group / GroupMembership / GroupNesting 或外部群组适配 | 统一主体、关系和继承语义 |
| Menu / MenuService / MenuSpecifications | system.navigation.NavigationItem / NavigationService | 导航数据与授权解耦 |
| Dict / SimpleDict / SimpleDictClassCache | system.dictionary.DictionaryDefinition / DictionaryEntry / DictionaryService | 可选功能包；平面条目为默认，树型目录按需绑定层级；删除实体扫描判别缓存 |
| AppConfig / ConfigItem / AppConfigUtil | system.configuration.ApplicationConfiguration / ConfigurationItem / ConfigurationService | 显式类型与服务 |
| PageElement / UserPageElement / SysrolePageElement / PageElementService | system.presentation 的 UiElementDefinition / UiElementPolicy / UiCapabilityService | ALLOW/DENY 按原用途迁移为呈现约束；业务能力另行绑定 |
| ViewConfig / ViewColumn | system.presentation 中的视图定义 | JSON 结构版本化、字段编码迁移 |
| BaseFile / SprFile / *Attachment / SortedFile | file 的 FileMetadata / AttachmentReference | 元数据和业务关联分离 |
| StorageService / FileStorageFactory | storage.api.StorageProvider / file.StorageProviderRegistry | 存储 SPI 与文件业务拆分 |
| FilePermissionService / DownloadPermission | file 应用服务调用 security-api，并按接入模式组合数据约束与授权管理端口 | 包含授权查询/授予/撤销及管理页面，统一授权来源 |
| 两处 UserService / UserDto / 用户管理适配 | identity-api 的目录/生命周期端口与 DTO、identity-local 默认实现、system.identity 管理服务 | 合并接口；角色通过可替换 provider 管理；本地凭据哈希存储，外部凭据委托身份源 |
| HibernateListener / Insert、Update、DeleteEventListener | 无对应运行时类型 | 授权前移至应用服务 |
| EntityManagerProvider / SpringContextUtil | 无对应公共类型 | 使用构造器注入与持久化上下文 |
| 两处 JsonUtils / Converter / JpaUtils / EntityUtil | 所属功能内部 mapper/converter/metadata | 去重；不发布万能工具入口 |
| ApiResponse / 通用错误对象 | web 的响应 DTO / ApiError | 契约明确，统一异常映射 |
| TablePrefix 两份定义 | 显式 @Table/@Column 与迁移脚本 | 删除重复常量和失效命名策略 |
| dto 中 OWL 类型 | OWL 所属模块 | 从通用基础层移出，由调用方依赖领域模块 |

## 6. 装配、schema 和发布

- 自动配置按功能条件启用；模块缺失不扫描其实体或 Repository。启动校验资源+scope 的数据执行模式、投影、存储 provider 和已启用模块的 schema 版本，失败给出明确原因。
- 引入单一版本化数据库迁移机制；使用仓库内可审查的脚本，由部署流水线单独执行。具体工具与 SQL 方言在部署数据库盘点后选定。
- 生产禁止 Hibernate 自动 update schema；应用只校验新 schema。迁移失败不启动应用。
- 每个物理表、列、索引和关联表显式命名；可以保留仍然合适的物理表名，这不属于兼容代码。名称变化必须由脚本完成。
- 新安装与旧库迁移后必须得到等价 schema。迁移工具不进入应用镜像或 starter 依赖树。
- 采用停写切换，无旧新版混跑。恢复策略是恢复匹配的旧数据库备份和旧应用版本，不是在新库上运行旧应用。

## 7. 完成标准

新版生产依赖中无两个旧 artifact、旧包或兼容层；调用方不再传类名；数据库不存在未处理的旧类型标识；授权、查询、层级与文件测试通过；迁移在真实数据库副本演练成功；前后端和任务完成整体验收。

本文确定架构和权限语义，实施时按这些决策建立测试与迁移。数据库方言、实际 schema、外部调用方、自定义字典字段、旧数据权限表达式、运维窗口和性能阈值仍需实施前采集。它们是发布前置条件，不通过运行时兼容逻辑兜底。

## 8. 复盘结论与实现收敛

前面各项是已确定的目标能力，按以下顺序实施和验证；它们不是待选择的设计方案：

| 优先级 | 事项 | 验证结果要求 |
| --- | --- | --- |
| P0 | 三种接入模式及依赖隔离 | 仅 RBAC 示例不包含数据策略 JAR、实体、表或配置要求 |
| P0 | 同一行的字段条件与多角色来源 | 测试证明不产生行集合与字段集合的交叉放权 |
| P0 | 资源与实体解耦 | 一个业务资源可以对应聚合或 DTO；注册实体不自动发布 CRUD |
| P0 | 查询/写入执行边界 | 全部入口落实约束；未支持的关联、表达式明确失败 |
| P0 | 更新前后与并发 | 归属转移、字段 patch、版本冲突和批量回滚符合契约 |
| P1 | 策略管理与撤权 | 发布校验、委派范围、版本审计、撤权时限可测试 |
| P1 | 组织/租户事实 | 接口预留可选命名空间；需要多租户的宿主强制应用边界，不硬编码企业目录 |
| P1 | 性能与可解释性 | 无逐行权限查询；关联规则优先 EXISTS 等可控计划，确定真实数据库索引和查询数预算 |

第一个纵向样例确定为订单：resource+scope 的 RBAC → 本人/本部门行条件 → 行条件下的字段读写 → 自定义冻结状态限制 → 新增/修改/列表/详情/导出。建立“仅 RBAC”“RBAC+数据策略”“增加自定义约束”三个独立装配测试。

首个实现里程碑完成标量条件、受控身份/组织事实、指定记录及显式字段读写。任意脚本规则、远程策略服务、自动推断全部关联权限、通用数据库适配和可视化规则编排不纳入首版。

实施顺序确定为：先完成订单安全链路；其后按目标应用实际使用情况完成层级、字典、文件等业务功能迁移。字典不阻塞仅接入安全框架的首版交付。层级采用第 4.5 节的 Hierarchy/Node/Edge 模型，TREE 与 DAG 按模式施加不同约束，不再将模型选择列为待定事项。阶段性开发不是旧新版混跑；生产切换前必须完成所选应用所用功能及其数据迁移，未完成的应用不切换，不加入历史兼容运行时。

### 8.1 决策与执行状态的区别

本文中的模块边界、授权来源组合、行/字段合并、缺省拒绝、接入模式、扩展限制、撤权边界和实施顺序均为确定决策。测试负责验证这些决策的实现，不在实施时重新选择语义。

真实数据库版本/schema、实际外部调用方、历史自定义类型与字段数据、运维窗口和性能基线属于尚未采集的事实。它们必须在编写及执行生产迁移前核实，不能通过把设计文档标为“已确定”来假定已完成。迁移清单中的未勾选项代表未完成的工作，不代表架构待决。

### 数据库权限目录

功能权限目录统一通过 security-api 的 `PermissionCatalog` 读取，RBAC、导航、页面元素和会话能力不再注入启动时固定的 `Set<PermissionRequest>`。JPA 实现位于 rbac-jpa，使用 `spr_permission_resource` 与 `spr_permission_scope`；宿主需要注册目录 Bean 并在数据库建表。目录按 namespace 隔离，resource/scope 编码不可修改，管理使用版本校验，首版不缓存目录或授权结果。

未知或停用权限拒绝执行与新增授权；停用权限仍可查询与撤销已有授权。恢复启用会恢复保留的授权。目录定义不替代业务接口检查，也不替代 JpaResource 的实体/字段映射。角色授权继续使用 spr_role_permission。

Sparrow admin 初始化内置目录，仅新增不存在的定义，不覆盖管理员更改；管理员离线初始化在目录初始化后执行。目录管理使用独立 admin.permission-catalog read/manage 权限与审计。新平台不提供旧配置兼容入口或历史数据迁移。shuita 使用 Flyway V26 建立目录与角色授权表，销售行/金额规则由业务表持久化，用户和角色保留 Keycloak；功能鉴权重新解析当前角色 ID。
