package com.bs_enterprises.enterprise_backend_template.services.impl;

import com.bs_enterprises.enterprise_backend_template.constants.CryptoProperties;
import com.bs_enterprises.enterprise_backend_template.services.CryptoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("CallToPrintStackTrace")
@Service
@RequiredArgsConstructor
public class AesGcmCryptoService implements CryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;     // 16 bytes tag
    private static final int IV_LENGTH_BYTES = 12;   // 96-bit IV recommended for GCM
    private static final String VERSION = "v1";

    private final CryptoProperties properties;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SecretKey> keyById = new HashMap<>();
    private String primaryKeyId;

    @PostConstruct
    void init() {
        if (properties.getKeys() == null || properties.getKeys().isEmpty()) {
            throw new IllegalStateException("No crypto keys configured under app.crypto.keys");
        }

        // The first key is considered primary (used for encryption)
        CryptoProperties.Key first = properties.getKeys().get(0);
        primaryKeyId = Objects.requireNonNull(first.getId(), "Primary key id must not be null");

        for (CryptoProperties.Key k : properties.getKeys()) {
            String id = Objects.requireNonNull(k.getId(), "key id must not be null");
            String secretB64 = Objects.requireNonNull(k.getSecret(), "key secret must not be null");
            byte[] raw = Base64.getDecoder().decode(secretB64);
            if (raw.length != 32) {
                throw new IllegalStateException("Key " + id + " must be 32 bytes (256-bit) after Base64 decode");
            }
            keyById.put(id, new SecretKeySpec(raw, "AES"));
        }
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) return null;

        SecretKey key = keyById.get(primaryKeyId);
        if (key == null) {
            throw new IllegalStateException("Primary key not loaded: " + primaryKeyId);
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // (Optional) AAD: include a stable context string if you want binding, e.g., table/field
            // cipher.updateAAD("hrms:v1".getBytes(StandardCharsets.UTF_8));

            byte[] cipherBytes = cipher.doFinal(plainBytes);

            return String.join(":",
                    VERSION,
                    primaryKeyId,
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(cipherBytes)
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String packedCiphertext) {
        if (packedCiphertext == null) return null;

        try {
            String[] parts = packedCiphertext.split(":");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid ciphertext format");
            }
            String version = parts[0];
            String keyId = parts[1];
            byte[] iv = Base64.getDecoder().decode(parts[2]);
            byte[] cipherBytes = Base64.getDecoder().decode(parts[3]);

            if (!VERSION.equals(version)) {
                throw new IllegalArgumentException("Unsupported crypto version: " + version);
            }
            SecretKey key = keyById.get(keyId);
            if (key == null) {
                throw new IllegalArgumentException("Unknown keyId: " + keyId);
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // If you used AAD on encrypt, you must set the same here
            // cipher.updateAAD("hrms:v1".getBytes(StandardCharsets.UTF_8));

            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Decryption failed", e);
        }
    }

}
