package com.changsha.zhongdian.operationlog;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

/**
 * 在开发库执行统一操作日志迁移脚本并验证表存在。
 * 为什么：本机没有 mysql 命令行客户端，通过 Spring Boot 真实数据源 + ResourceDatabasePopulator
 * 执行 SQL 迁移文件，确保 migrations/ 中的脚本可在目标开发库正确运行。
 */
@SpringBootTest
@ActiveProfiles("local")
public class OperationLogMigrationExecutionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void migrationScriptShouldCreateOperationLogTable() throws SQLException {
        // 使用文件系统路径读取迁移脚本，与 schema.sql 校验测试保持相同口径
        FileSystemResource migrationScript = new FileSystemResource("../db/migrations/20260908_create_biz_operation_log.sql");

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(migrationScript);
        populator.setSeparator(";");
        populator.execute(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            ResultSet tables = connection.getMetaData()
                    .getTables(connection.getCatalog(), null, "biz_operation_log", null);
            assertTrue(tables.next(), "执行迁移脚本后开发库中应存在 biz_operation_log 表");
        }
    }
}
