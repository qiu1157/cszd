package com.changsha.zhongdian.shareholding;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class EquityAdjustmentEventSchemaSqlTest {

    @Test
    void schemaShouldDefineEquityAdjustmentEventTableWithCoreConstraints() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS biz_equity_adjustment_event"),
                        "应定义 biz_equity_adjustment_event 股权调整事件表"),
                () -> assertTrue(schemaSql.contains("before_snapshot TEXT NOT NULL"),
                        "应定义调整前快照字段且必填"),
                () -> assertTrue(schemaSql.contains("after_snapshot TEXT NOT NULL"),
                        "应定义调整后快照字段且必填"),
                () -> assertTrue(schemaSql.contains("total_percentage DECIMAL(5,2) NOT NULL"),
                        "应定义占比合计字段并锁定精度"),
                () -> assertTrue(schemaSql.contains("operator_account VARCHAR(32) DEFAULT NULL"),
                        "应定义操作人账号字段且允许为空"),
                () -> assertTrue(schemaSql.contains("KEY idx_biz_equity_adjustment_event_station_id (station_id)"),
                        "应为充电站ID建立检索索引"),
                () -> assertTrue(schemaSql.contains("KEY idx_biz_equity_adjustment_event_created_at (created_at)"),
                        "应为创建时间建立时间线检索索引")
        );
    }

    @Test
    void schemaShouldDocumentEquityAdjustmentEventBusinessIntent() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("股权调整事件表"),
                        "应包含股权调整事件表注释"),
                () -> assertTrue(schemaSql.contains("历史变化时间线"),
                        "应说明为时间线提供数据来源"),
                () -> assertTrue(schemaSql.contains("由应用层维护引用一致性"),
                        "应说明不做物理外键的口径"),
                () -> assertTrue(schemaSql.contains("二次确认"),
                        "应说明合计非100需保存前二次确认")
        );
    }
}