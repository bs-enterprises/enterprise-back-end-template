package com.bs_enterprises.enterprise_backend_template.services.base.impl;

import com.bs_enterprises.enterprise_backend_template.config.KeycloakProperties;
import com.bs_enterprises.enterprise_backend_template.models.keycloak.BulkOperationPayload;
import com.bs_enterprises.enterprise_backend_template.models.keycloak.BulkOperationResponse;
import com.bs_enterprises.enterprise_backend_template.services.base.KeycloakOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

/**
 * Implementation of KeycloakOperationsService for bulk user operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakOperationsServiceImpl implements KeycloakOperationsService {

    private final WebClient webClient;

    private final KeycloakProperties keycloakProperties;


    private static final String BULK_OPERATIONS_BASE = "/realms/%s/user-bulk";
    private static final String BULK_DEACTIVATE = "/bulk-deactivate";
    private static final String BULK_ENABLE = "/bulk-enable";
    private static final String BULK_DELETE = "/bulk-delete";

    private static final String REALM_NAME_REQUIRED = "realmName required";
    private static final String PAYLOAD_REQUIRED = "payload required";
    private static final String TOKEN_REQUIRED = "token required";

    @Override
    public String getValidAccessToken(String tenant) {
        Objects.requireNonNull(tenant, "tenant required");
        log.debug("Fetching valid access token for tenant={}", tenant);

        try {
            // Get token from configured master client
            // In production, this would:
            // 1. Check if cached token is still valid (not expired)
            // 2. If expired, refresh by calling Keycloak token endpoint
            // 3. Cache token with expiration time
            // 4. Return valid token

            String token = generateAccessTokenFromMasterClient(tenant);
            log.debug("Valid access token obtained for tenant={}", tenant);
            return token;
        } catch (Exception ex) {
            log.error("Failed to get valid access token for tenant={}: {}", tenant, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to get valid access token: " + ex.getMessage(), ex);
        }
    }

    /**
     * Generate access token using master client credentials
     * In production, this would call Keycloak /token endpoint
     */
    private String generateAccessTokenFromMasterClient(String tenant) {
        try {
            // This is a placeholder implementation
            // In real code, this would:
            // 1. Get master client from keycloakProperties.getMasterClient("web")
            // 2. Call Keycloak token endpoint with client_id and client_secret
            // 3. Parse response and return access_token
            // 4. Cache token with expiration

            log.debug("Generating token for Keycloak realm: {}, tenant: {}",
                    keycloakProperties.getRealm(), tenant);

            // Placeholder return - replace with actual Keycloak token endpoint call
            return "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.placeholder_token";
        } catch (Exception ex) {
            log.error("Failed to generate Keycloak token: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Failed to generate Keycloak token: " + ex.getMessage(), ex);
        }
    }

    @Override
    public BulkOperationResponse bulkDeactivate(String realmName, BulkOperationPayload payload, String token) {
        Objects.requireNonNull(realmName, REALM_NAME_REQUIRED);
        Objects.requireNonNull(payload, PAYLOAD_REQUIRED);
        Objects.requireNonNull(token, TOKEN_REQUIRED);

        try {
            BulkOperationResponse response = performBulkOperation(
                    BULK_DEACTIVATE,
                    realmName,
                    payload,
                    token
            );
            log.info("Bulk deactivate completed for realm '{}': {} success, {} not found",
                    realmName, response.getSuccessCount(), response.getNotFoundCount());
            return response;
        } catch (Exception ex) {
            log.error("Bulk deactivate failed for realm '{}': {}", realmName, ex.getMessage(), ex);
            throw new IllegalStateException("Bulk deactivate operation failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public BulkOperationResponse bulkEnable(String realmName, BulkOperationPayload payload, String token) {
        Objects.requireNonNull(realmName, REALM_NAME_REQUIRED);
        Objects.requireNonNull(payload, PAYLOAD_REQUIRED);
        Objects.requireNonNull(token, TOKEN_REQUIRED);

        try {
            BulkOperationResponse response = performBulkOperation(
                    BULK_ENABLE,
                    realmName,
                    payload,
                    token
            );
            log.info("Bulk enable completed for realm '{}': {} success, {} not found",
                    realmName, response.getSuccessCount(), response.getNotFoundCount());
            return response;
        } catch (Exception ex) {
            log.error("Bulk enable failed for realm '{}': {}", realmName, ex.getMessage(), ex);
            throw new IllegalStateException("Bulk enable operation failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public BulkOperationResponse bulkDelete(String realmName, BulkOperationPayload payload, String token) {
        Objects.requireNonNull(realmName, REALM_NAME_REQUIRED);
        Objects.requireNonNull(payload, PAYLOAD_REQUIRED);
        Objects.requireNonNull(token, TOKEN_REQUIRED);

        try {
            BulkOperationResponse response = performBulkOperation(
                    BULK_DELETE,
                    realmName,
                    payload,
                    token
            );
            log.info("Bulk delete completed for realm '{}': {} success, {} not found",
                    realmName, response.getSuccessCount(), response.getNotFoundCount());
            return response;
        } catch (Exception ex) {
            log.error("Bulk delete failed for realm '{}': {}", realmName, ex.getMessage(), ex);
            throw new IllegalStateException("Bulk delete operation failed: " + ex.getMessage(), ex);
        }
    }

    // ----------------------------
    // Helper methods
    // ----------------------------

    /**
     * Perform bulk operation against Keycloak API
     */
    private BulkOperationResponse performBulkOperation(
            String endpoint,
            String realmName,
            BulkOperationPayload payload,
            String token) {

        String basePath = String.format(BULK_OPERATIONS_BASE, realmName);
        String url = keycloakProperties.getServerUrl() + basePath + endpoint;

        try {
            BulkOperationResponse response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(BulkOperationResponse.class)
                    .block();

            if (response == null) {
                throw new IllegalStateException("No response received from Keycloak bulk operation");
            }

            return response;
        } catch (WebClientResponseException ex) {
            log.error("Keycloak API error (status={}): {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException(
                    "Keycloak bulk operation failed with HTTP " + ex.getStatusCode() + ": " +
                            ex.getResponseBodyAsString(),
                    ex
            );
        }
    }
}
