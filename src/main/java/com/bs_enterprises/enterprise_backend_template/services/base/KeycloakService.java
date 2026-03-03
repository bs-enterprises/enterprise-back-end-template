package com.bs_enterprises.enterprise_backend_template.services.base;

import com.bs_enterprises.enterprise_backend_template.models.keycloak.*;
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
    // 🔹 User, Client & Role Management (internal/realm setup)
    // ---------------------------------------------------------------
    void disableDefaultRequiredActions(Keycloak realmKeycloak, String realmName);

    void createClient(Keycloak realmKeycloak, String realmName, String clientId, String name, String description, boolean isConfidential);

    void assignRealmManagementRoles(Keycloak realmKeycloak, String realmName);

    String createAdminUser(Keycloak realmKeycloak, String realmName, String username, String email);

    void setPassword(Keycloak realmKeycloak, String realmName, String userId, String password);

    void assignAdminRole(Keycloak realmKeycloak, String realmName, String userId);

    Keycloak assignClientRolesToMasterUser(String newRealm);

    // ---------------------------------------------------------------
    // 🔹 Roles Management APIs
    // ---------------------------------------------------------------

    /** Get all clients and their roles in a realm */
    RealmClientsAndRoles getClientsAndRoles(String realmName);

    /** Check if a client exists in a realm */
    boolean clientExists(String realmName, String clientId);

    /** Check if a role exists for a specific client in a realm */
    boolean roleExists(String realmName, String clientId, String roleName);

    /** Create a new client in a realm from a ResourceModel */
    ClientRoleInfo createNewClient(String realmName, ResourceModel resource);

    /** Update an existing client in a realm from a ResourceModel */
    void updateClient(String realmName, ResourceModel resource);

    /** Delete a client from a realm by clientId */
    void deleteClient(String realmName, String clientId);

    /** Add a role to a client in a realm from a RoleModel */
    void addRoleToClient(String realmName, String clientId, RoleModel role);

    /** Update an existing role in a client from a RoleModel */
    void updateRoleInClient(String realmName, String clientId, RoleModel role);

    /** Remove a role from a client in a realm */
    void removeRoleFromClient(String realmName, String clientId, String roleName);

    /** Assign client roles to a specific user */
    UserClientRolesInfo assignRolesToUser(String realmName, UserClientRolesCarrier carrier);

    /** Remove client roles from a specific user */
    UserClientRolesInfo removeRolesFromUser(String realmName, UserClientRolesCarrier carrier);

    /** Get all client roles assigned to a specific user */
    UserClientRolesInfo getUserClientRoles(String realmName, String userId, String clientId);
}
