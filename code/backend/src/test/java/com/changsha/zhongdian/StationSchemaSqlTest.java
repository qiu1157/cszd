package com.changsha.zhongdian;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class StationSchemaSqlTest {

    @Test
    void schemaShouldDefineChargingStationTableWithCoreConstraints() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS charging_station"),
                        "应定义 charging_station 充电站表"),
                () -> assertTrue(schemaSql.contains("system_id VARCHAR(32) NOT NULL"),
                        "应定义系统ID字段"),
                () -> assertTrue(schemaSql.contains("station_name VARCHAR(128) NOT NULL"),
                        "应定义充电站名称字段"),
                () -> assertTrue(schemaSql.contains("platform_dict_item_id BIGINT NOT NULL"),
                        "应定义所在平台字典项字段"),
                () -> assertTrue(schemaSql.contains("platform_station_id VARCHAR(64) DEFAULT NULL"),
                        "应定义平台ID字段且允许为空"),
                () -> assertTrue(schemaSql.contains("station_status_dict_item_id BIGINT NOT NULL"),
                        "应定义充电站状态字典项字段"),
                () -> assertTrue(schemaSql.contains("contact_name VARCHAR(64) DEFAULT NULL"),
                        "应定义联系人字段"),
                () -> assertTrue(schemaSql.contains("contact_phone VARCHAR(20) DEFAULT NULL"),
                        "应定义联系电话字段"),
                () -> assertTrue(schemaSql.contains("station_address VARCHAR(255) DEFAULT NULL"),
                        "应定义地址字段"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_charging_station_system_id (system_id)"),
                        "应保证系统ID全局唯一"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_charging_station_platform_station_id (platform_dict_item_id, platform_station_id)"),
                        "应保证同一平台内平台ID唯一"),
                () -> assertTrue(schemaSql.contains("CONSTRAINT fk_charging_station_platform_dict_item_id FOREIGN KEY (platform_dict_item_id) REFERENCES sys_dict_item (id)"),
                        "应定义所在平台到系统字典项的外键"),
                () -> assertTrue(schemaSql.contains("CONSTRAINT fk_charging_station_status_dict_item_id FOREIGN KEY (station_status_dict_item_id) REFERENCES sys_dict_item (id)"),
                        "应定义站点状态到系统字典项的外键")
        );
    }

    @Test
    void schemaShouldDocumentStationLifecycleAndBusinessIntent() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("充电站表"),
                        "应包含充电站表注释"),
                () -> assertTrue(schemaSql.contains("系统自动生成，且全局唯一"),
                        "应说明系统ID生成规则"),
                () -> assertTrue(schemaSql.contains("平台ID在同一平台内唯一"),
                        "应说明平台ID唯一规则"),
                () -> assertTrue(schemaSql.contains("签合同"),
                        "应说明默认状态与生命周期范围")
        );
    }
}
