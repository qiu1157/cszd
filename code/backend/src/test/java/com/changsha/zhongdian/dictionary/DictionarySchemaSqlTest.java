package com.changsha.zhongdian.dictionary;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class DictionarySchemaSqlTest {

    @Test
    void schemaShouldDefineSystemDictionaryTablesWithCoreConstraints() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS sys_dict_type"),
                        "应定义 sys_dict_type 字典类型表"),
                () -> assertTrue(schemaSql.contains("type_code VARCHAR(64) NOT NULL"),
                        "应定义类型编码字段"),
                () -> assertTrue(schemaSql.contains("type_name VARCHAR(64) NOT NULL"),
                        "应定义类型名称字段"),
                () -> assertTrue(schemaSql.contains("type_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'"),
                        "应定义类型状态字段"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_sys_dict_type_code (type_code)"),
                        "应保证类型编码唯一"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_sys_dict_type_name (type_name)"),
                        "应保证类型名称唯一"),
                () -> assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS sys_dict_item"),
                        "应定义 sys_dict_item 字典项表"),
                () -> assertTrue(schemaSql.contains("type_id BIGINT NOT NULL"),
                        "应定义字典类型ID字段"),
                () -> assertTrue(schemaSql.contains("item_code VARCHAR(64) NOT NULL"),
                        "应定义字典项编码字段"),
                () -> assertTrue(schemaSql.contains("item_name VARCHAR(64) NOT NULL"),
                        "应定义字典项名称字段"),
                () -> assertTrue(schemaSql.contains("item_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED'"),
                        "应定义字典项状态字段"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_sys_dict_item_type_code (type_id, item_code)"),
                        "应保证同一类型下编码唯一"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_sys_dict_item_type_name (type_id, item_name)"),
                        "应保证同一类型下名称唯一"),
                () -> assertTrue(schemaSql.contains("CONSTRAINT fk_sys_dict_item_type_id FOREIGN KEY (type_id) REFERENCES sys_dict_type (id)"),
                        "应定义字典项到字典类型的外键")
        );
    }

    @Test
    void schemaShouldMentionBuiltinSystemDictionaryScope() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("系统字典类型表"),
                        "应包含字典类型表注释"),
                () -> assertTrue(schemaSql.contains("系统字典项表"),
                        "应包含字典项表注释"),
                () -> assertTrue(schemaSql.contains("PLATFORM / STATION_STATUS / PILE_STATUS"),
                        "应在表结构注释中说明第一版内置类型")
        );
    }
}
