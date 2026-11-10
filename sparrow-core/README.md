# 无框架查询值对象

`cn.sparrowmini.core.query.Condition` 是通用查询与权限条件复用的类型化布尔条件，不依赖 Spring、JPA 或授权模块。条件只描述数据，不产生访问资格。字段名由执行适配器对注册表校验；字段扫描限制深度 64、节点总数 10000。

支持 All、None、Equal、In、And、Or、Not。数据库编译器显式处理 null，使内存判定与 SQL 结果一致。没有字符串代码执行或兼容旧过滤语法的解析器。
