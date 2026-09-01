package com.changsha.zhongdian.pile;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class PileSchemaSqlTest {

    @Test
    void schemaShouldDefineChargingPileTableWithCoreConstraints() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS biz_pile"),
                        "应定义 biz_pile 充电桩表"),
                () -> assertTrue(schemaSql.contains("pile_code VARCHAR(32) NOT NULL"),
                        "应定义桩编号字段"),
                () -> assertTrue(schemaSql.contains("station_id BIGINT NOT NULL"),
                        "应定义所属充电站字段"),
                () -> assertTrue(schemaSql.contains("charge_type VARCHAR(16) NOT NULL"),
                        "应定义充电类型字段"),
                () -> assertTrue(schemaSql.contains("gun_count INT NOT NULL"),
                        "应定义充电枪数量字段"),
                () -> assertTrue(schemaSql.contains("pile_status_item_id BIGINT NOT NULL"),
                        "应定义充电桩状态字典项字段"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_biz_pile_pile_code (pile_code)"),
                        "应保证桩编号全局唯一"),
                () -> assertTrue(schemaSql.contains("CONSTRAINT fk_biz_pile_station_id FOREIGN KEY (station_id) REFERENCES biz_station (id)"),
                        "应定义所属充电站外键"),
                () -> assertTrue(schemaSql.contains("CONSTRAINT fk_biz_pile_pile_status_item_id FOREIGN KEY (pile_status_item_id) REFERENCES sys_dict_item (id)"),
                        "应定义充电桩状态到系统字典项的外键")
        );
    }

    @Test
    void schemaShouldDocumentPileLifecycleAndBusinessIntent() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("充电桩表"),
                        "应包含充电桩表注释"),
                () -> assertTrue(schemaSql.contains("桩编号由系统自动生成，且全局唯一"),
                        "应说明桩编号生成规则"),
                () -> assertTrue(schemaSql.contains("biz_gun 表以 pile_id 关联扩展"),
                        "应说明枪级扩展预留方式"),
                () -> assertTrue(schemaSql.contains("管理粒度为桩级"),
                        "应说明当前管理粒度为桩级"),
                () -> assertTrue(schemaSql.contains("运营中 / 运营异常 / 停用"),
                        "应说明充电桩生命周期范围"),
                () -> assertTrue(schemaSql.contains("至少为 1"),
                        "应说明充电枪数量下限规则")
        );
    }
}
