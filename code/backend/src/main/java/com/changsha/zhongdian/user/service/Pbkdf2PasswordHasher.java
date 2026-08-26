package com.changsha.zhongdian.user.service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

@Component
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final String VALUE_SEPARATOR = ":";

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String hash(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        byte[] derivedKey = deriveKey(rawPassword, salt, ITERATIONS);
        return ITERATIONS
                + VALUE_SEPARATOR
                + Base64.getEncoder().encodeToString(salt)
                + VALUE_SEPARATOR
                + Base64.getEncoder().encodeToString(derivedKey);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        String[] parts = hashedPassword.split(VALUE_SEPARATOR);
        if (parts.length != 3) {
            return false;
        }

        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8));
        byte[] actualHash = Base64.getDecoder().decode(parts[2].getBytes(StandardCharsets.UTF_8));
        byte[] expectedHash = deriveKey(rawPassword, salt, iterations);

        return java.security.MessageDigest.isEqual(expectedHash, actualHash);
    }

    private byte[] deriveKey(String rawPassword, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, KEY_LENGTH);
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            return secretKeyFactory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("当前运行环境不支持 PBKDF2 密码哈希", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
