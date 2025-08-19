package cn.sparrowmini.common;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;

public class PrefixPhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Value("${sparrow.hibernate.table.prefix:}")
    private static String PREFIX; // 可以从配置文件读取，甚至动态刷新

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        String tableName = name.getText();

        // 排除某些固定表（例如 sys_ 开头的不加前缀）
        if (tableName.startsWith("spr_")) {
            return name;
        }

        return Identifier.toIdentifier(PREFIX + tableName);
    }

    // 其它方法保持默认实现
    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) { return name; }
    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) { return name; }
    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) { return name; }
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) { return name; }
}