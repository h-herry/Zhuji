package com.zhuji.userorg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class ConfigEncryptionService {

    private static final List<String> SENSITIVE_PREFIXES = Arrays.asList(
        "password", "apiKey", "secret", "token", "credential", "key", "auth"
    );

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    @Value("${config.encryption.key}")
    private String encryptionKey;

    public boolean isSensitive(String configKey) {
        if (configKey == null) return false;
        String lowerKey = configKey.toLowerCase();
        return SENSITIVE_PREFIXES.stream()
            .anyMatch(lowerKey::contains);
    }

    public String encrypt(String value) {
        if (value == null) return null;
        try {
            // 生成随机IV
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec secretKey = new SecretKeySpec(
                encryptionKey.getBytes(StandardCharsets.UTF_8), "AES"
            );
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // 将IV和密文合并
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedValue);

            // 分离IV和密文
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec secretKey = new SecretKeySpec(
                encryptionKey.getBytes(StandardCharsets.UTF_8), "AES"
            );
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}
