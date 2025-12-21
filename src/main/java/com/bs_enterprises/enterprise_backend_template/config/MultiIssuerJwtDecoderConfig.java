package com.bs_enterprises.enterprise_backend_template.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Predicate;

@Configuration
public class MultiIssuerJwtDecoderConfig {

    private final KeycloakProperties keycloakProperties;

    public MultiIssuerJwtDecoderConfig(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    @Bean
    public MultiIssuerJwtDecoder jwtDecoder() {
        // Allowed issuer predicate: restrict to realms hosted on your Keycloak server
        String baseUrl = keycloakProperties.getServerUrl(); // e.g. https://auth-dev.mindzage.com

        // Accept issuers that start with the base server URL + "/realms/"
        Predicate<String> allowed = issuer ->
                issuer != null && issuer.startsWith(baseUrl + "/realms/");

        // Cache lifespan: 30 minutes
        Duration ttl = Duration.ofMinutes(30);

        return new MultiIssuerJwtDecoder(allowed, ttl);
    }
}
