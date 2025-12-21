package com.bs_enterprises.enterprise_backend_template.services;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Service;

/**
 * Interface defining Keycloak realm and user management operations.
 */
@Service
public interface KeycloakService {

    // ---------------------------------------------------------------
    // 🔹 Realm CRUD-Like Operations
    // ---------------------------------------------------------------
    void createRealm(String realmName);

    RealmRepresentation getRealm(String realmName);

    void updateRealm(String realmName, RealmRepresentation updatedRealm);

    void deleteRealm(String realmName);

    void safeDeleteRealm(String realmName);

    // ---------------------------------------------------------------
    // 🔹 User, Client & Role Management
    // ---------------------------------------------------------------
    void disableDefaultRequiredActions(Keycloak realmKeycloak, String realmName);

    void createClient(Keycloak realmKeycloak, String realmName, String clientId,String name,String description, boolean isConfidential);

    void assignRealmManagementRoles(Keycloak realmKeycloak, String realmName);

    String createAdminUser(Keycloak realmKeycloak, String realmName, String username, String email);

    void setPassword(Keycloak realmKeycloak, String realmName, String userId, String password);

    void assignAdminRole(Keycloak realmKeycloak, String realmName, String userId);

    Keycloak assignClientRolesToMasterUser(String newRealm);
}
