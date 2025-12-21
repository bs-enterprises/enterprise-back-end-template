package com.bs_enterprises.enterprise_backend_template.services;

import com.bs_enterprises.enterprise_backend_template.models.users.KeycloakUserModel;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface KeycloakUserService {

    /**
     * Create a Keycloak user in the given realm for the studio and return created user id.
     * realmName is treated as the tenant. studioId is stored as user attribute "studioId".
     */
    String createUser(String realmName, KeycloakUserModel model);

    /**
     * Get a user by id and ensure it belongs to the studio (studioId attribute).
     */
    UserRepresentation getUser(String realmName, String studioId, String userId);

    /**
     * Update a user by id (overwrites fields from model) and keep studio attribute unchanged.
     */
    void updateUser(String realmName, String userId, KeycloakUserModel model);

    /**
     * Delete a user by id after verifying studio ownership.
     */
    void deleteUser(String realmName, String userId);

    /**
     * Search users (Keycloak search param). Results are filtered by studioId.
     */
    List<UserRepresentation> searchUsers(String realmName, String search, int first, int max);

    /**
     * Find users by username (fuzzy). Results are filtered by studioId.
     */
    List<UserRepresentation> findByUsername(String realmName, String username, int first, int max);

    /**
     * Set password for a user (non-temporary by default) after verifying studio ownership.
     */
    void setPassword(String realmName, String studioId, String userId, String password, boolean temporary);
}
