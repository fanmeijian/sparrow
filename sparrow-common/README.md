# sparrow-common

Sparrow 的公共模型与持久化基础库，为业务应用提供基础实体、树结构、权限关系、字典、配置、文件元数据和数据传输约定。当前 Maven 坐标为 `cn.sparrowmini:sparrow-common:2.1.0`，产物为 JAR，使用 Java 17。

本模块需要 JPA/Hibernate 等依赖，不是纯 DTO 工具包，也不是可以独立启动的应用。对应的 Repository 和 Spring 服务位于 [sparrow-common-service](../sparrow-common-service/README.md)。

## 模块关系

```text
业务应用（认证、HTTP API、业务规则、数据库配置）
    └── sparrow-common-service（Spring 服务、Repository、查询和权限检查）
            └── sparrow-common（实体、DTO、视图、监听器和基础工具）
```

业务应用可以直接依赖 common 复用模型；需要公共服务时，由 common-service 传递引入本模块。

## 功能组成

| 功能      | 主要类型                                                               | 实现与用途                                                |
| ------- | ------------------------------------------------------------------ | ---------------------------------------------------- |
| 审计字段    | `BaseOpLog`、`BasOpLogListener`                                     | 保存创建/修改时间和操作人；时间由 Hibernate 维护，操作人从 `CurrentUser` 获取 |
| 状态与基础实体 | `BaseState`、`BaseEntity`、`BaseUuidEntity`                          | 提供业务状态、单据状态、启用/隐藏标记、模型名及 UUID 主键等基础字段                |
| 基础资料    | `BaseDataEntity`、`BaseDataUuidEntity`                              | 为资料类实体提供公共模型；前者以 `code` 为主键                          |
| 树结构     | `BaseTree`、`BaseTreeV2`、对应监听器                                      | 分别表达单父节点和多父关系，提供排序及前端树展示字段                           |
| 字典与配置   | `Dict`、`SimpleDict`、`AppConfig`、`ConfigItem`、`AppConfigAttachment` | 定义字典、配置项和配置附件的持久化模型                                  |
| 权限与组织   | `Menu`、`Scope`、`Model`、`ModelAttribute`、`PageElement`、`model.pem`  | 定义菜单、操作范围、模型/字段、页面元素及用户、角色、分组授权关系                    |
| 文件元数据   | `BaseFile`、`SprFile`、`BaseAttachment`、`BaseSortedFile`             | 描述文件和附件关系；实际上传下载由存储实现承担                              |
| 数据传输    | `dto`、`view`、`ApiResponse`                                         | 提供 DTO、接口投影和响应包装                                     |
| 基础工具    | `JsonUtils`、`JpaUtils`、`EntityUtil`、JSON Converter                 | 支持 JSON 转换、实体主键处理和 JSON 属性持久化                        |

权限实体表达的是授权数据结构，不能仅凭继承这些实体就获得全部权限校验；服务层和业务入口仍需接入对应检查。

## 实体架构

主要继承链为：

```text
BaseOpLog
    └── BaseState
          └── BaseEntity
                ├── BaseUuidEntity
                │     ├── BaseTree
                │     └── BaseTreeV2
                └── BaseDataEntity
```

这些基础类型通过 `@MappedSuperclass` 将字段映射继承给具体业务实体。`BaseUuidEntity.id` 使用 Hibernate UUID 生成；`BaseOpLog` 的时间字段使用 `@CreationTimestamp` 和 `@UpdateTimestamp`，操作人通过 JPA 生命周期回调填充。

部分字段使用 Jackson `READ_ONLY`，例如审计字段、UUID 主键和 `stat`。通过 JSON 更新实体时，应遵守这些字段的读写约定。

### 两种树模型

- `BaseTree`：单个 `parentId`，节点自身保存 `seq` 排序值。
- `BaseTreeV2`：通过 `@ElementCollection` 保存 `Set<ParentTree>`；每条关系包含 `parentId` 和 `seq`，同一节点可在不同父节点下拥有独立排序。
- 两者都包含名称、编码、目录、描述及 `includeAllChildren` 等字段；`children`、`childCount`、`level` 等用于查询结果或界面展示。
- 两者的查询、移动和级联删除服务位于 common-service；V2 是不同的关系模型，迁移时需要处理数据结构变化。

## 当前用户与生命周期

`CurrentUser.logIn(UserInfo)` 将用户名和用户信息写入线程上下文；`get()`、`getUserInfo()` 供审计和权限逻辑读取；`logOut()` 清理上下文。

宿主应用负责认证后设置上下文，并在请求结束的 `finally` 中清理。线程池和异步任务需要显式处理上下文传递，不能依赖普通 `ThreadLocal` 自动继承。

树监听器引用 `EntityManagerProvider.entityManagerFactory`；使用相关功能前，需要由宿主应用初始化该引用。它是静态全局状态，多持久化单元场景应检查适用性。审计字段记录的是创建及最近修改信息，不等同于完整的操作历史表。

## 接入与构建

```xml
<dependency>
    <groupId>cn.sparrowmini</groupId>
    <artifactId>sparrow-common</artifactId>
    <version>2.1.0</version>
</dependency>
```

宿主应用需要配置数据源、持久化单元和实体扫描，并按需提供当前用户上下文。主要依赖包括 Hibernate ORM、Jackson、Commons、Lombok、Reflections；MVEL 在 POM 中标记为 `provided`。

在本目录构建：

```bash
mvn clean install
```

该命令将产物安装到本地 Maven 仓库，供 common-service 使用。

## 源码导航与当前约束

- `src/main/java/cn/sparrowmini/common/model`：实体及基础映射。
- `model/pem`：权限关系；`model/pem/group`：分组关系。
- `dto`、`view`：传输对象和接口视图。
- `listener`：JPA 生命周期监听器。
- `converter`、`util`、`constant`：转换、工具和枚举。

当前 `PrefixPhysicalNamingStrategy.java` 全部为注释，不能将其配置为有效 Hibernate 命名策略；旧文档中的 `sparrow.hibernate.table.prefix` 也不能据此视为已实现配置。代码中的 `TablePrefix.NAME` 为 `spr_`。common-service 当前还存在相同完整类名的 `TablePrefix`，后续应统一定义。

## 新版重构规划

当前 README 描述现有实现。新版目标见 [重构架构设计](../docs/refactoring/architecture.md) 和 [迁移清单](../docs/refactoring/migration-checklist.md)。新版不保留运行时兼容层，通过调用方升级及独立数据库迁移完成适配。
