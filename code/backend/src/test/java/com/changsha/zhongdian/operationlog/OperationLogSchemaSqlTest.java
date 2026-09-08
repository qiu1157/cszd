package com.changsha.zhongdian.operationlog;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class OperationLogSchemaSqlTest {

    @Test
    void schemaShouldDefineOperationLogTableWithCoreConstraints() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS biz_operation_log"),
                        "应定义 biz_operation_log 统一操作日志表"),
                () -> assertTrue(schemaSql.contains("operator_account VARCHAR(32)"),
                        "应定义操作人账号字段"),
                () -> assertTrue(schemaSql.contains("operator_role VARCHAR(32)"),
                        "应定义操作人角色字段"),
                () -> assertTrue(schemaSql.contains("target_type VARCHAR(32) NOT NULL"),
                        "应定义对象类型字段且必填"),
                () -> assertTrue(schemaSql.contains("operation_type VARCHAR(32) NOT NULL"),
                        "应定义操作类型字段且必填"),
                () -> assertTrue(schemaSql.contains("before_content TEXT"),
                        "应定义修改前内容字段"),
                () -> assertTrue(schemaSql.contains("after_content TEXT"),
                        "应定义修改后内容字段"),
                () -> assertTrue(schemaSql.contains("station_id BIGINT"),
                        "应定义关联充电站ID字段用于按站点筛选")
        );
    }

    @Test
    void schemaShouldDefineOperationLogIndexes() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("KEY idx_biz_operation_log_created_at (created_at)"),
                        "应为创建时间建立索引以支撑时间线倒序查询"),
                () -> assertTrue(schemaSql.contains("KEY idx_biz_operation_log_operator_account (operator_account)"),
                        "应为操作人账号建立索引以支撑按操作人筛选"),
                () -> assertTrue(schemaSql.contains("KEY idx_biz_operation_log_target_type (target_type)"),
                        "应为对象类型建立索引以支撑按对象类型筛选"),
                () -> assertTrue(schemaSql.contains("KEY idx_biz_operation_log_station_id (station_id)"),
                        "应为充电站ID建立索引以支撑按充电站筛选")
        );
    }

    @Test
    void schemaShouldDocumentOperationLogBusinessIntent() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("统一操作日志表"),
                        "应包含统一操作日志表注释"),
                () -> assertTrue(schemaSql.contains("全局日志页"),
                        "应说明为全局日志页提供数据"),
                () -> assertTrue(schemaSql.contains("时间线"),
                        "应说明为时间线提供数据"),
                () -> assertTrue(schemaSql.contains("非站点操作可为空"),
                        "应说明 station_id 允许为空")
        );
    }
}
