package com.bs_enterprises.enterprise_backend_template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Data
@Component
@ConfigurationProperties(prefix = "properties.keycloak")
public class KeycloakProperties {

    private String serverUrl;

    private String realm;

    private String username;
    private String password;
    /**
     * master-clients:
     *   mobile:
     *     id: ...
     *     secret: ...
     *   web:
     *     id: ...
     *     secret: ...
     */
    private Map<String, ConfidentialClient> masterClients = Collections.emptyMap();

    /**
     * token-issuers:
     *   mobile:
     *     id: ...
     *   web:
     *     id: ...
     */
    private Map<String, PublicClient> tokenIssuers = Collections.emptyMap();

    /**
     * Convenience method: get master client by key (e.g. "mobile" or "web")
     */
    public Optional<ConfidentialClient> getMasterClient(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(masterClients.get(key));
    }

    /**
     * Convenience method: get token issuer by key
     */
    public Optional<PublicClient> getTokenIssuer(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(tokenIssuers.get(key));
    }

    @Data
    public static class ConfidentialClient {
        private String id;
        private String secret;
    }

    @Data
    public static class PublicClient {
        private String id;
    }
}
