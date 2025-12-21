package com.bs_enterprises.enterprise_backend_template.services;

import com.bs_enterprises.enterprise_backend_template.models.users.KeycloakUserModel;
import com.bs_enterprises.enterprise_backend_template.models.users.LoadedArtifacts;

import java.util.List;
import java.util.Map;

public interface UserAccountService {

    /**
     * Ensure id exists (generate if missing) and perform uniqueness checks for uid/email/phone.
     * Returns the final id to use.
     */
    String prepareAndValidateIdentifiers(String realmName,
                                         String userId,
                                         String email,
                                         String phone,
                                         List<String> studioIds,
                                         boolean studioRequired);

    /**
     * Provision a new user:
     * - create user in Keycloak
     * - persist KeycloakUserModel via repository
     * - persist UserSecrets via UserSecretService
     * - create index entries for uid/email/phone
     */
    void provisionNewUser(String realmName, KeycloakUserModel kcUser);

    /**
     * Load essentials for a user: KeycloakUserModel and UserSecrets. Throws if missing.
     */
    LoadedArtifacts loadArtifacts(String realmName, String userId);

    /**
     * Apply updates to kcUser, manage index updates for email/phone and persist.
     * AllowedKeys controls which kcUser fields this method will accept updates for.
     * Returns the persisted KeycloakUserModel.
     */
    KeycloakUserModel updateUser(String realmName,
                                 String userId,
                                 Map<String, Object> updates,
                                 List<String> allowedKeys);

    /**
     * Delete common user resources: delete in Keycloak, delete indices, remove stored user doc and secrets.
     */
    void deleteUser(String realmName, String userId);

}
