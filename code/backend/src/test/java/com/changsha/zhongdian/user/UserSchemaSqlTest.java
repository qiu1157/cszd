package com.changsha.zhongdian.user;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class UserSchemaSqlTest {

    @Test
    void schemaShouldDefineSystemUserTableWithCoreConstraints() throws IOException {
        String schemaSql = Files.readString(Path.of("..", "db", "schema.sql"));

        assertAll(
                () -> assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS sys_user"),
                        "应定义 sys_user 用户表"),
                () -> assertTrue(schemaSql.contains("account VARCHAR(32) NOT NULL"),
                        "应定义登录账号字段"),
                () -> assertTrue(schemaSql.contains("phone VARCHAR(20)"),
                        "应定义手机号字段"),
                () -> assertTrue(schemaSql.contains("password_hash VARCHAR(255) NOT NULL"),
                        "应定义密码哈希字段"),
                () -> assertTrue(schemaSql.contains("role_code VARCHAR(32) NOT NULL"),
                        "应定义角色字段"),
                () -> assertTrue(schemaSql.contains("user_status VARCHAR(16) NOT NULL"),
                        "应定义状态字段"),
                () -> assertTrue(schemaSql.contains("is_default_admin TINYINT(1) NOT NULL DEFAULT 0"),
                        "应定义默认管理员标记字段"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_sys_user_account (account)"),
                        "应保证登录账号唯一"),
                () -> assertTrue(schemaSql.contains("UNIQUE KEY uk_sys_user_phone (phone)"),
                        "应保证手机号唯一")
        );
    }
}
