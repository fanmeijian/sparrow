# sparrow-common-service

Sparrow 的公共业务服务与 JPA 访问层，基于 [sparrow-common](../sparrow-common/README.md) 的实体模型，提供通用 CRUD、动态条件查询、投影、树操作、字典与权限管理，以及文件存储扩展接口。

当前 Maven 坐标为 `cn.sparrowmini:sparrow-common-service:2.1.0`。本模块是供宿主应用集成的 JAR，不包含独立运行所需的完整 HTTP API、认证入口或数据源配置。

## 技术与依赖

- Java 17；Spring Boot `3.5.5` BOM 管理部分依赖版本。
- Spring Data JPA、Hibernate ORM `6.6.26.Final`，使用 Jakarta Persistence API。
- Jackson 负责实体更新和类型转换，AspectJ 支持权限切面。
- ANTLR `4.13.1` 将过滤表达式语法生成 Lexer、Parser 和 Visitor。
- `sparrow-common:2.1.0` 提供公共模型；MVEL 为 `provided` 依赖。

引入 Boot BOM 负责版本管理，不会自动完成应用启动和所有 Bean 的装配。

## 实现架构

```text
业务 Controller / 业务服务
    ├── CommonJpaService ── EntityManager ── 数据库
    ├── 树、字典、菜单、角色等服务
    │      └── BaseRepository / 专用 Repository
    │             └── CustomRepositoryFactoryBean
    │                    └── CustomRepositoryFactory
    │                           └── BaseRepositoryImpl ── EntityManager
    └── StorageService ── 宿主提供的存储实现

查询：filter → ANTLR Parser → ExprToPredicateVisitor → Criteria Predicate
操作权限：@ScopePermission → ScopePermissionAspect → ScopeRepository
模型写入权限：Hibernate PRE_* 事件 → 事件监听器 → ModelPermissionService
```

通用 EntityManager 服务和自定义 Repository 是两条并存的访问路径，接入时应明确选择，并验证各自的更新与查询语义。

## 功能与主要入口

| 功能 | 入口 | 实现说明 |
| --- | --- | --- |
| 通用数据访问 | `CommonJpaService` / `CommonJpaServiceImpl` | 根据实体 Class 执行保存、Map 更新、新增或更新、删除、按 ID 查询、分页及去重字段查询 |
| Repository 扩展 | `BaseRepository` / `BaseRepositoryImpl` | 扩展 `JpaRepository`、`JpaSpecificationExecutor`，增加 upsert、投影和实体元数据访问 |
| 动态投影 | `DynamicProjectionHelper`、`ProjectionHelper` 等 | 按投影类型构造查询结果；Repository 工厂提供 `ProjectionFactory` |
| 树结构 | `CommonTreeService`、`CommonTreeServiceV2` 及实现 | 查询子节点/后代、移动节点、保存、级联删除；分别使用对应树 Repository |
| 字典 | `SimpleDictService`、`SimpleDictClassCache` | 按实体类型或 discriminator 操作简单字典，支持分页和投影 |
| 菜单和授权管理 | `MenuService`、`ScopeService`、`SysroleService`、`GroupService` 等 | 管理公共权限数据及菜单访问关系 |
| 模型写入权限 | `ModelPermissionService`、Hibernate 事件监听器 | 在实体插入、修改、删除事件中调用模型权限逻辑 |
| 配置 | `AppConfigUtil`、配置相关 Repository | 提供配置查询与数据访问相关能力 |
| 文件存储扩展 | `StorageService`、`FileStorageFactory` | 定义上传、下载、删除、元数据查询；按 `StorageTypeEnum` 选择实现 |

`UserService`、`FilePermissionService`、`ScopePermissionService` 等类型是接口，不能把接口声明视为本模块已提供完整实现。`JpaService`、`BaseJpaService`、`InheritTableJpaService` 等抽象也与 `CommonJpaService` 并存，使用前需检查具体调用路径。

## 查询与更新流程

### 动态过滤

语法位于 `src/main/antlr/Expr.g4`，`PredicateBuilder` 和 `ExprToPredicateVisitor` 将表达式转换为 JPA Criteria 条件。语法支持：

- `and`、`or`、`not` 和括号。
- `=`、`!=`、`<`、`<=`、`>`、`>=`、`like`。
- `is null`、`is not null`、`in (...)`。
- 点分隔字段路径、单引号字符串、数字和布尔值。

例如，针对包含对应字段的实体：

```text
name like '设备%' and enabled = true
code in ('A', 'B')
parentId is null
```

当前语法关键字为小写，数字 token 未包含负号；表达式不是任意 SQL。字段和类型还需要满足实体映射及 Visitor 的处理规则。

分页通过 `Pageable` 传入；`CommonJpaServiceImpl` 使用 Criteria 查询、分页参数和计数查询返回 `Page`。投影查询由相应 Helper 处理。

### 保存与更新

`saveEntity()` 调用 `persist()`，应按新增语义使用。`upsertEntity()` 读取主键，尝试获取实体引用并使用 Jackson 合并 Map；在指定异常分支中创建实体。Repository 的 `upsertAll()` 是另一套实现，不能假定两条路径完全等价。

宿主应用应验证请求字段、可修改范围和业务状态。部分公共字段已有 Jackson 只读约束，但通用 Map 更新不能替代业务校验。当前 service upsert 使用异常判断新增，参数错误与实体不存在的边界仍需谨慎处理。

## 树结构

`CommonTreeServiceImpl` 对应单父节点 `BaseTree`；`CommonTreeServiceV2Impl` 对应多父关系 `BaseTreeV2`。服务根据实体类型寻找对应 Repository，再委托查询、移动和删除。

V2 的每条父关系有独立排序值。当前 V2 移动实现将父 ID 转为 `String`，使用时应与 UUID 字符串主键保持一致。级联删除的影响范围取决于 Repository 实现，多父节点场景需要专门验证。

## 权限检查与边界

### 操作范围权限

`@ScopePermission` 由 `ScopePermissionAspect` 拦截，读取 `CurrentUser` 中的用户名和角色，通过 `ScopeRepository` 判断权限：

1. 用户或角色匹配授权时允许执行。
2. 已配置该 scope 的授权，但当前用户和角色不匹配时拒绝。
3. 未配置该 scope 的用户及角色授权时，当前实现默认允许。

调用前应建立用户上下文；该切面直接访问用户角色。切面依赖 Spring AOP 代理调用，不应假定任意对象调用都能触发检查。

### 模型写入权限

`HibernateListener` 初始化时向 Hibernate 注册 PRE_INSERT、PRE_UPDATE、PRE_DELETE 监听器，由相应监听器调用 `ModelPermissionService`。该服务使用独立 EntityManager 查询权限。

这条路径依赖 Hibernate 事件机制，不能据此推断原生 SQL、批量 DML 或读取查询自动受到同样保护。当前插入监听器在缺少当前用户时跳过权限调用，并排除部分权限模型。权限策略和具体查询实现应结合业务验证；当前模型权限服务中用于检查允许配置的查询仍传入 `DENY`，不应将其描述为已经验证完整的允许/拒绝策略。

## 接入方式

```xml
<dependency>
    <groupId>cn.sparrowmini</groupId>
    <artifactId>sparrow-common-service</artifactId>
    <version>2.1.0</version>
</dependency>
```

宿主应用负责以下装配：

1. 配置数据源、EntityManagerFactory、事务管理和实体扫描，包含实际使用的公共实体及业务实体。
2. 扫描公共服务和监听器，提供依赖的 Repository；启用 AOP 以使用操作权限切面。
3. 使用 `CustomRepositoryFactoryBean` 创建公共扩展 Repository，使其由 `BaseRepositoryImpl` 实现。
4. 在认证入口设置 `CurrentUser`，在请求结束时清理；使用树监听器时初始化 common 中的 `EntityManagerProvider.entityManagerFactory`。
5. 使用文件功能时提供 `StorageService` Bean，以及业务所需的权限接口实现。

Spring Data Repository 配置示意（需替换业务包名）：

```java
@EnableJpaRepositories(
    basePackages = {"cn.sparrowmini.common.repository", "com.example.repository"},
    repositoryFactoryBeanClass = CustomRepositoryFactoryBean.class
)
```

上例只展示 Repository 工厂配置，实体扫描、组件扫描和数据源仍由宿主配置。两个模块共享 `cn.sparrowmini.common` 包空间，扫描范围需要结合使用功能确定。

`FileStorageFactory` 收集全部 `StorageService` Bean，并按存储类型建立映射。当前重复类型会覆盖，未注册类型会返回 null，接入方应保证注册完整且唯一。

## 构建与源码导航

从仓库根目录依次执行：

```bash
mvn -f sparrow-common/pom.xml clean install
mvn -f sparrow-common-service/pom.xml clean verify
```

先安装 common，避免 service 使用本地仓库中的旧模型。ANTLR 插件在 `generate-sources` 阶段生成解析代码；不要直接修改 `target` 下的生成文件。

- `service`：公共服务、权限切面及扩展接口。
- `repository`：Repository 接口、工厂、基础实现及查询辅助类。
- `antlr` 与 `src/main/antlr`：表达式转换和语法。
- `listener`：Hibernate 事件注册与写入权限监听。
- `dto`、`exception`：服务数据对象与异常。

当前两个模块未发现测试源码。接入验证应优先覆盖权限配置矩阵、upsert 的新增和更新分支、过滤与投影、树移动及级联删除。上述构建命令是使用说明，不代表本文档已记录构建通过结果。

## 新版重构规划

当前 README 描述现有实现。新版目标见 [重构架构设计](../docs/refactoring/architecture.md) 和 [迁移清单](../docs/refactoring/migration-checklist.md)。新版不保留运行时兼容层，通过调用方升级及独立数据库迁移完成适配。
