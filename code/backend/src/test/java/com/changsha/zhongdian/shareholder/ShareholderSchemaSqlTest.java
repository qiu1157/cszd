package com.changsha.zhongdian.shareholder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class ShareholderSchemaSqlTest {

    @Test
    void schemaShouldDefineShareholderTableWithCoreConstraints() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS biz_shareholder"),
                        "应定义 biz_shareholder 股东主档表"),
                () -> assertTrue(schemaSql.contains("shareholder_name VARCHAR(128) NOT NULL"),
                        "应定义股东名称字段"),
                () -> assertTrue(schemaSql.contains("contact_name VARCHAR(64) DEFAULT NULL"),
                        "应定义联系人字段且允许为空"),
                () -> assertTrue(schemaSql.contains("contact_phone VARCHAR(20) DEFAULT NULL"),
                        "应定义联系电话字段且允许为空"),
                () -> assertTrue(schemaSql.contains("shareholder_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'"),
                        "应定义股东状态字段并默认启用"),
                () -> assertTrue(schemaSql.contains("KEY idx_biz_shareholder_shareholder_name (shareholder_name)"),
                        "应为股东名称建立检索索引"),
                () -> assertTrue(schemaSql.contains("KEY idx_biz_shareholder_shareholder_status (shareholder_status)"),
                        "应为股东状态建立筛选索引")
        );
    }

    @Test
    void schemaShouldDocumentShareholderLifecycleAndBusinessIntent() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("股东主档表"),
                        "应包含股东主档表注释"),
                () -> assertTrue(schemaSql.contains("独立主档实体"),
                        "应说明股东为独立主档实体"),
                () -> assertTrue(schemaSql.contains("退股不属于主档状态"),
                        "应说明退股不属于主档状态"),
                () -> assertTrue(schemaSql.contains("不引入字典项"),
                        "应说明股东状态不引入字典项"),
                () -> assertTrue(schemaSql.contains("第一期不做物理删除"),
                        "应说明第一期不做物理删除")
        );
    }
}