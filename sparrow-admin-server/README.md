# Sparrow 独立管理服务器

Java 17 / Spring Boot 3.5.5。此宿主装配本地账号、角色、群组、文件存储、可选字典和数据策略管理；核心模块仍允许业务项目自行选择身份和角色 provider。此宿主自身采用本地身份，Keycloak 项目可使用独立 keycloak-spring-boot-starter 装配，不能直接把此本地宿主当成已切换的 Keycloak 管理宿主。

从仓库根目录构建：

```sh
mvn -f sparrow-platform/pom.xml package
```

配置环境变量 `SPARROW_DATABASE_URL`、`SPARROW_DATABASE_USERNAME`、`SPARROW_DATABASE_PASSWORD`、`SPARROW_STORAGE_ROOT`。当前打包内置 H2 驱动；其他数据库驱动由部署方加入，不能仅更换 URL 后声称已经验收了其他数据库。

首次管理员引导只允许非 Web 进程，要求账号命名空间为空。通过环境设置 `SPARROW_BOOTSTRAP_USERNAME` 和 `SPARROW_BOOTSTRAP_PASSWORD`，然后运行：

```sh
java -jar sparrow-admin-server/target/sparrow-admin-server-3.0.0-SNAPSHOT.jar \
  --spring.main.web-application-type=none --sparrow.bootstrap.enabled=true
```

运行时默认使用 `ddl-auto=validate`。仅在专门的新建、空白开发数据库上，可为上述初始化命令添加 `--spring.jpa.hibernate.ddl-auto=create` 创建新模型；不要用于已有数据的数据库。历史数据库迁移不属于本轮实施范围。

引导完成后清除两个引导凭据环境变量，正常启动：

```sh
java -jar sparrow-admin-server/target/sparrow-admin-server-3.0.0-SNAPSHOT.jar
```

访问 `/sparrow-admin/index.html`，未登录时转到 Spring Security 登录页。管理页面按当前功能权限显示入口，写入使用 CSRF token。管理员角色只获得初始化时已注册的管理权限，不隐式拥有业务文件范围，也不自动获得将来新增的权限。

本宿主将 `admin.*:manage` 定义为命名空间级管理权，包含授权管理全权。仅应授予可信管理员；业务项目需为受限授权管理员提供更细的委派 guard，不能把这里的全权管理语义当作有限委派实现。

文件管理提供上传、元数据、下载、重命名、引用查看、标记删除与执行删除。有引用文件拒绝删除；业务附件绑定要求项目提供真实父对象授权适配器，本宿主不接受客户端伪造业务上下文。字典删除也保持关闭，直到项目接入其业务引用检查器；停用条目可用于历史显示。

测试包括 H2 事务、真实密码登录、匿名/普通用户拒绝、CSRF、版本冲突、角色撤销、密码重置失效、策略草稿/发布与静态资源保护。浏览器视觉验收尚未完成，不能以 HTTP 测试替代。
