package com.bs_enterprises.enterprise_backend_template.services.base;

import com.bs_enterprises.enterprise_backend_template.models.keycloak.BulkOperationPayload;
import com.bs_enterprises.enterprise_backend_template.models.keycloak.BulkOperationResponse;

/**
 * Service for Keycloak bulk operations (deactivate, enable, delete)
 */
public interface KeycloakOperationsService {

    /**
     * Get a valid access token for the given tenant/realm
     * Handles token refresh if expired
     * @param tenant the tenant/realm identifier
     * @return valid access token
     */
    String getValidAccessToken(String tenant);

    /**
     * Bulk deactivate users in Keycloak
     * @param realmName the Keycloak realm name
     * @param payload contains list of identifiers to deactivate
     * @param token authorization token for the Keycloak realm
     * @return BulkOperationResponse with success/failure details
     */
    BulkOperationResponse bulkDeactivate(String realmName, BulkOperationPayload payload, String token);

    /**
     * Bulk enable users in Keycloak
     * @param realmName the Keycloak realm name
     * @param payload contains list of identifiers to enable
     * @param token authorization token for the Keycloak realm
     * @return BulkOperationResponse with success/failure details
     */
    BulkOperationResponse bulkEnable(String realmName, BulkOperationPayload payload, String token);

    /**
     * Bulk delete users in Keycloak
     * @param realmName the Keycloak realm name
     * @param payload contains list of identifiers to delete
     * @param token authorization token for the Keycloak realm
     * @return BulkOperationResponse with success/failure details
     */
    BulkOperationResponse bulkDelete(String realmName, BulkOperationPayload payload, String token);
}
