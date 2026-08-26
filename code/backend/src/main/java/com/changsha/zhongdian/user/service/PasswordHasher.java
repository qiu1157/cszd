package com.changsha.zhongdian.user.service;

public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
