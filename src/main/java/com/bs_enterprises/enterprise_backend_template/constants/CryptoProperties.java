package com.bs_enterprises.enterprise_backend_template.constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = PropertiesConfigPrefixes.APP_CRYPTO)
public class CryptoProperties {

    private List<Key> keys;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Key {
        private String id;      // e.g., "primary"
        private String secret;  // Base64-encoded 32 bytes
    }
}
