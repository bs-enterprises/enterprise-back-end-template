package com.bs_enterprises.enterprise_backend_template.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KeycloakConfig {

    private final KeycloakProperties keycloakProperties;

    public KeycloakConfig(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    /**
     * This bean is the Keycloak Admin Client.
     * It's used for administrative tasks like creating, deleting, or managing users,
     * roles, and groups from your backend. It uses its own admin credentials.
     */
    @Bean
    public Keycloak keycloakAdminClient() {
        log.info("Initializing Keycloak Admin Client for realm: {}", keycloakProperties.getRealm());

        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getRealm()) // Admin client often authenticates against the master realm
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli") // Use the standard admin-cli client
                .username(keycloakProperties.getUsername())
                .password(keycloakProperties.getPassword())
                .build();
    }
}
