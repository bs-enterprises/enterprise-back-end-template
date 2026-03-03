package com.bs_enterprises.enterprise_backend_template.services.base.impl;

import com.bs_enterprises.enterprise_backend_template.config.KeycloakProperties;
import com.bs_enterprises.enterprise_backend_template.models.keycloak.*;
import com.bs_enterprises.enterprise_backend_template.services.base.KeycloakService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.*;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private static final String DEFAULT_ROLE = "realm-admin";
    private static final String REALM_MANAGEMENT_CLIENT = "realm-management";

    private final Keycloak keycloak; // master realm Keycloak client
    private final KeycloakProperties keycloakProperties;

    // ---------------------------------------------------------------
    // 🔹 Realm CRUD-Like Operations
    // ---------------------------------------------------------------

    /**
     * ✅ Create a new realm
     */
    @Override
    public void createRealm(String realmName) {
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(realmName);
        realm.setEnabled(true);
        keycloak.realms().create(realm);
        log.info("✅ Created realm: {}", realmName);
    }

    /**
     * 🔍 Get realm by name
     */
    @Override
    public RealmRepresentation getRealm(String realmName) {
        return keycloak.realm(realmName).toRepresentation();
    }

    /**
     * 🔄 Update realm details
     */
    @Override
    public void updateRealm(String realmName, RealmRepresentation updatedRealm) {
        keycloak.realm(realmName).update(updatedRealm);
        log.info("🔄 Updated realm: {}", realmName);
    }

    /**
     * ❌ Delete realm by name
     */
    @Override
    public void deleteRealm(String realmName) {
        keycloak.realm(realmName).remove();
        log.info("🧨 Deleted realm: {}", realmName);
    }

    /**
     * 🧩 Safe delete: wraps delete in try-catch
     */
    @Override
    public void safeDeleteRealm(String realmName) {
        try {
            deleteRealm(realmName);
        } catch (Exception e) {
            log.warn("⚠️ Failed to delete realm {} (may not exist): {}", realmName, e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 🔹 User, Client & Role Management
    // ---------------------------------------------------------------

    @Override
    public void disableDefaultRequiredActions(Keycloak realmKeycloak, String realmName) {
        List<RequiredActionProviderRepresentation> actions =
                realmKeycloak.realm(realmName).flows().getRequiredActions();
        for (RequiredActionProviderRepresentation action : actions) {
            if (action.isEnabled()) {
                action.setEnabled(false);
                realmKeycloak.realm(realmName).flows().updateRequiredAction(action.getAlias(), action);
                log.info("⚙️ Disabled required action: {}", action.getAlias());
            }
        }
    }

    @Override
    public void createClient(Keycloak realmKeycloak, String realmName, String clientId, String name, String description, boolean isConfidential) {
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setProtocol("openid-connect");
        client.setName(name);
        client.setDescription(description);
        client.setEnabled(true);

        boolean publicClient = !isConfidential;
        // optional: limited use for confidential clients
        boolean standardFlow = !isConfidential;

        // Apply directly
        client.setPublicClient(publicClient);
        client.setServiceAccountsEnabled(isConfidential);
        client.setDirectAccessGrantsEnabled(isConfidential);
        client.setImplicitFlowEnabled(isConfidential);
        client.setStandardFlowEnabled(standardFlow);

        // Only confidential clients need a secret
        if (isConfidential) {
            client.setSecret(generateRandomClientSecret());
        }

        // Create the client in the realm
        Response response = realmKeycloak.realm(realmName).clients().create(client);

        // Log result and attempt to extract created client id from Location header
        if (response.getStatus() == 201 || response.getStatus() == 204) {
            String location = response.getHeaderString("Location");
            if (location != null) {
                String createdId = location.substring(location.lastIndexOf('/') + 1);
                log.info("✅ Created {} client '{}' in realm: {} (id={})",
                        isConfidential ? "confidential" : "public", clientId, realmName, createdId);
            } else {
                log.info("✅ Created {} client '{}' in realm: {} (no Location header returned)",
                        isConfidential ? "confidential" : "public", clientId, realmName);
            }
        } else {
            log.warn("⚠️ Failed to create client '{}'. HTTP status: {}. Response: {}",
                    clientId, response.getStatus(), response.getStatusInfo());
        }

        response.close();
    }


    @Override
    public void assignRealmManagementRoles(Keycloak realmKeycloak, String realmName) {
        String clientId = realmKeycloak.realm(realmName).clients()
                .findByClientId(REALM_MANAGEMENT_CLIENT).getFirst().getId();

        String[] roles = {"manage-users", "manage-clients", "view-users", "view-realm", "manage-realm"};
        for (String role : roles) {
            log.info("✅ Prepared realm-management role: {}", role);
        }
    }

    @Override
    public String createAdminUser(Keycloak realmKeycloak, String realmName, String username, String email) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);

        Response response = realmKeycloak.realm(realmName).users().create(user);
        if (response.getStatus() != 201) {
            throw new IllegalStateException("❌ Failed to create admin user. Status: " + response.getStatus());
        }

        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        log.info("✅ Created admin user '{}' with ID '{}' in realm: {}", username, userId, realmName);
        return userId;
    }

    @Override
    public void setPassword(Keycloak realmKeycloak, String realmName, String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        realmKeycloak.realm(realmName).users().get(userId).resetPassword(credential);
        log.info("✅ Set password for admin user '{}'", userId);
    }

    @Override
    public void assignAdminRole(Keycloak realmKeycloak, String realmName, String userId) {
        String clientId = realmKeycloak.realm(realmName).clients()
                .findByClientId(REALM_MANAGEMENT_CLIENT).get(0).getId();

        RoleRepresentation adminRole = realmKeycloak.realm(realmName)
                .clients().get(clientId).roles().get(DEFAULT_ROLE).toRepresentation();

        realmKeycloak.realm(realmName).users().get(userId)
                .roles().clientLevel(clientId)
                .add(Collections.singletonList(adminRole));

        log.info("✅ Assigned '{}' role to user '{}' in realm: {}", DEFAULT_ROLE, userId, realmName);
    }

    @Override
    public Keycloak assignClientRolesToMasterUser(String newRealm) {
        String masterRealm = "master";
        String username = keycloakProperties.getUsername();

        List<UserRepresentation> users = keycloak.realm(masterRealm).users().search(username);
        if (users.isEmpty()) throw new IllegalStateException("❌ Master user not found: " + username);
        String userId = users.getFirst().getId();

        List<ClientRepresentation> clients = keycloak.realm(masterRealm)
                .clients().findByClientId(newRealm + "-realm");
        if (clients.isEmpty()) {
            log.warn("⚠️ Client not found for new realm: {}", newRealm + "-realm");
            return null;
        }

        String clientId = clients.get(0).getId();
        List<RoleRepresentation> availableRoles = keycloak.realm(masterRealm)
                .users().get(userId).roles().clientLevel(clientId).listAvailable();

        if (availableRoles.isEmpty()) return null;

        keycloak.realm(masterRealm).users().get(userId)
                .roles().clientLevel(clientId).add(availableRoles);

        log.info("✅ Assigned {} client-level roles to master-admin for new realm '{}'",
                availableRoles.size(), newRealm);

        KeycloakProperties.ConfidentialClient masterClient = keycloakProperties.getMasterClient(newRealm).get();


        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(masterRealm)
                .clientId(masterClient.getId())
                .clientSecret(masterClient.getSecret())
                .username(keycloakProperties.getUsername())
                .password(keycloakProperties.getPassword())
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }

    @Override
    public RealmClientsAndRoles getClientsAndRoles(String realmName) {
        log.info("Fetching all clients and roles for realm={}", realmName);

        List<ClientRepresentation> clients = keycloak.realm(realmName).clients().findAll();
        List<ClientRoleInfo> clientRoleInfoList = new ArrayList<>();

        for (ClientRepresentation client : clients) {
            List<String> clientRoles = keycloak.realm(realmName)
                    .clients().get(client.getId()).roles().list()
                    .stream().map(RoleRepresentation::getName).collect(Collectors.toList());

            clientRoleInfoList.add(ClientRoleInfo.builder()
                    .id(client.getId())
                    .clientId(client.getClientId())
                    .name(client.getName())
                    .description(client.getDescription())
                    .enabled(client.isEnabled())
                    .roles(clientRoles)
                    .build());
        }

        RealmClientsAndRoles result = RealmClientsAndRoles.builder()
                .realm(realmName)
                .clientCount(clientRoleInfoList.size())
                .clients(clientRoleInfoList)
                .build();

        log.info("Fetched {} clients with roles for realm={}", clientRoleInfoList.size(), realmName);
        return result;
    }

    @Override
    public boolean clientExists(String realmName, String clientId) {
        log.debug("Checking if client exists with clientId={} in realm={}", clientId, realmName);

        try {
            List<ClientRepresentation> existing = keycloak.realm(realmName).clients().findByClientId(clientId);
            boolean exists = !existing.isEmpty();
            log.debug("Client exists: {} for clientId={} in realm={}", exists, clientId, realmName);
            return exists;
        } catch (Exception e) {
            log.warn("Error checking client existence for clientId={} in realm={}: {}", clientId, realmName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean roleExists(String realmName, String clientId, String roleName) {
        log.debug("Checking if role exists with roleName={} for clientId={} in realm={}", roleName, clientId, realmName);

        try {
            // First, find the client by clientId
            List<ClientRepresentation> clients = keycloak.realm(realmName).clients().findByClientId(clientId);

            if (clients.isEmpty()) {
                log.debug("Client not found with clientId={} in realm={}, role cannot exist", clientId, realmName);
                return false;
            }

            String internalClientId = clients.get(0).getId();

            // Get all roles for the client
            List<RoleRepresentation> roles = keycloak.realm(realmName).clients().get(internalClientId).roles().list();

            // Check if the role exists
            boolean exists = roles.stream()
                    .anyMatch(role -> role.getName().equals(roleName));

            log.debug("Role exists: {} for roleName={} clientId={} in realm={}", exists, roleName, clientId, realmName);
            return exists;
        } catch (Exception e) {
            log.warn("Error checking role existence for roleName={} clientId={} in realm={}: {}",
                    roleName, clientId, realmName, e.getMessage());
            return false;
        }
    }

    @Override
    public ClientRoleInfo createNewClient(String realmName, ResourceModel resource) {
        log.info("Creating new client clientId={} in realm={}", resource.getId(), realmName);

        // Check existence first - if client already exists, return it without re-creating
        List<ClientRepresentation> existing = keycloak.realm(realmName).clients().findByClientId(resource.getId());
        if (!existing.isEmpty()) {
            return null;
        }

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(resource.getId());
        client.setName(resource.getName());
        client.setDescription(resource.getDescription());
        client.setEnabled(true);
        client.setProtocol("openid-connect");
        client.setPublicClient(true);
        client.setServiceAccountsEnabled(false);
        client.setDirectAccessGrantsEnabled(false);
        client.setStandardFlowEnabled(true);

        Response response = keycloak.realm(realmName).clients().create(client);
        response.close();

        if (response.getStatus() != 201 && response.getStatus() != 204) {
            throw new IllegalStateException("Failed to create client: " + resource.getId() + " status=" + response.getStatus());
        }

        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(resource.getId());
        if (found.isEmpty()) {
            throw new IllegalStateException("Client created but could not be fetched: " + resource.getId());
        }

        ClientRepresentation created = found.get(0);
        log.info("Client created successfully clientId={} id={} in realm={}", created.getClientId(), created.getId(), realmName);

        return ClientRoleInfo.builder()
                .id(created.getId())
                .clientId(created.getClientId())
                .name(created.getName())
                .description(created.getDescription())
                .enabled(created.isEnabled())
                .roles(new ArrayList<>())
                .build();
    }

    @Override
    public void deleteClient(String realmName, String clientId) {
        log.info("Deleting client clientId={} from realm={}", clientId, realmName);

        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(clientId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Client not found: " + clientId + " in realm=" + realmName);
        }

        String internalId = found.get(0).getId();
        keycloak.realm(realmName).clients().get(internalId).remove();

        log.info("Client deleted successfully clientId={} from realm={}", clientId, realmName);
    }

    @Override
    public void updateClient(String realmName, ResourceModel resource) {
        log.info("Updating client clientId={} in realm={}", resource.getId(), realmName);

        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(resource.getId());
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Client not found: " + resource.getId() + " in realm=" + realmName);
        }

        String internalId = found.get(0).getId();
        ClientRepresentation client = found.get(0);

        if (resource.getName() != null) client.setName(resource.getName());
        if (resource.getDescription() != null) client.setDescription(resource.getDescription());

        keycloak.realm(realmName).clients().get(internalId).update(client);

        log.info("Client updated successfully clientId={} in realm={}", resource.getId(), realmName);
    }

    @Override
    public void updateRoleInClient(String realmName, String clientId, RoleModel role) {
        log.info("Updating role id={} in clientId={} realm={}", role.getId(), clientId, realmName);

        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(clientId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Client not found: " + clientId + " in realm=" + realmName);
        }

        String internalId = found.get(0).getId();

        RoleRepresentation existing = keycloak.realm(realmName)
                .clients().get(internalId).roles().get(role.getId()).toRepresentation();

        if (existing == null) {
            throw new IllegalArgumentException("Role not found: " + role.getId() + " in clientId=" + clientId);
        }

        if (role.getRoleName() != null) existing.setName(role.getId());
        if (role.getDescription() != null) existing.setDescription(role.getDescription());

        keycloak.realm(realmName).clients().get(internalId).roles().get(role.getId()).update(existing);

        log.info("Role updated successfully id={} in clientId={} realm={}", role.getId(), clientId, realmName);
    }

    @Override
    public void addRoleToClient(String realmName, String clientId, RoleModel role) {
        log.info("Adding role id={} to clientId={} in realm={}", role.getId(), clientId, realmName);

        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(clientId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Client not found: " + clientId + " in realm=" + realmName);
        }

        String internalId = found.get(0).getId();

        // Check if role already exists - if so, skip creation
        boolean roleAlreadyExists = keycloak.realm(realmName)
                .clients().get(internalId).roles().list()
                .stream().anyMatch(r -> r.getName().equals(role.getId()));

        if (roleAlreadyExists) {
            log.info("Role already exists, skipping creation roleId={} in clientId={} realm={}", role.getId(), clientId, realmName);
            return;
        }

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(role.getId());
        roleRepresentation.setDescription(role.getDescription());

        keycloak.realm(realmName).clients().get(internalId).roles().create(roleRepresentation);

        log.info("Role added successfully roleId={} to clientId={} in realm={}", role.getId(), clientId, realmName);
    }

    @Override
    public void removeRoleFromClient(String realmName, String clientId, String roleName) {
        log.info("Removing role roleName={} from clientId={} in realm={}", roleName, clientId, realmName);

        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(clientId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Client not found: " + clientId + " in realm=" + realmName);
        }

        String internalId = found.get(0).getId();

        // Check if role exists - if not, skip removal silently
        boolean roleExists = keycloak.realm(realmName)
                .clients().get(internalId).roles().list()
                .stream().anyMatch(r -> r.getName().equals(roleName));

        if (!roleExists) {
            log.info("Role does not exist, skipping removal roleName={} in clientId={} realm={}", roleName, clientId, realmName);
            return;
        }

        keycloak.realm(realmName).clients().get(internalId).roles().get(roleName).remove();

        log.info("Role removed successfully roleName={} from clientId={} in realm={}", roleName, clientId, realmName);
    }

    @Override
    public UserClientRolesInfo assignRolesToUser(String realmName, UserClientRolesCarrier carrier) {
        log.info("Assigning roles={} of clientId={} to userId={} in realm={}", carrier.getRoleNames(), carrier.getClientId(), carrier.getUserId(), realmName);
        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(carrier.getClientId());
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Client not found: " + carrier.getClientId() + " in realm=" + realmName);
        }

        String internalClientId = found.get(0).getId();

        // Resolve role representations for only the requested role names
        List<RoleRepresentation> rolesToAssign = carrier.getRoleNames().stream()
                .map(roleName -> keycloak.realm(realmName).clients().get(internalClientId).roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());

        keycloak.realm(realmName).users().get(carrier.getUserId())
                .roles().clientLevel(internalClientId).add(rolesToAssign);

        // Return the full list of assigned roles after assignment
        List<String> assignedRoles = keycloak.realm(realmName).users().get(carrier.getUserId())
                .roles().clientLevel(internalClientId).listAll()
                .stream().map(RoleRepresentation::getName).collect(Collectors.toList());

        log.info("Roles assigned successfully userId={} clientId={} in realm={}", carrier.getUserId(), carrier.getClientId(), realmName);

        return UserClientRolesInfo.builder()
                .userId(carrier.getUserId())
                .clientId(carrier.getClientId())
                .assignedRoles(assignedRoles)
                .build();
    }

    @Override
    public UserClientRolesInfo removeRolesFromUser(String realmName, UserClientRolesCarrier carrier) {
        log.info("Removing roles={} of clientId={} from userId={} in realm={}", carrier.getRoleNames(), carrier.getClientId(), carrier.getUserId(), realmName);

        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(carrier.getClientId());
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Client not found: " + carrier.getClientId() + " in realm=" + realmName);
        }

        String internalClientId = found.get(0).getId();

        // Resolve role representations for only the requested role names
        List<RoleRepresentation> rolesToRemove = carrier.getRoleNames().stream()
                .map(roleName -> keycloak.realm(realmName).clients().get(internalClientId).roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());

        keycloak.realm(realmName).users().get(carrier.getUserId())
                .roles().clientLevel(internalClientId).remove(rolesToRemove);

        // Return the remaining assigned roles after removal
        List<String> remainingRoles = keycloak.realm(realmName).users().get(carrier.getUserId())
                .roles().clientLevel(internalClientId).listAll()
                .stream().map(RoleRepresentation::getName).collect(Collectors.toList());

        log.info("Roles removed successfully userId={} clientId={} in realm={}", carrier.getUserId(), carrier.getClientId(), realmName);

        return UserClientRolesInfo.builder()
                .userId(carrier.getUserId())
                .clientId(carrier.getClientId())
                .assignedRoles(remainingRoles)
                .build();
    }

    @Override
    public UserClientRolesInfo getUserClientRoles(String realmName, String userId, String clientId) {
        log.info("Getting client roles for userId={} clientId={} in realm={}", userId, clientId, realmName);

        List<ClientRepresentation> found = keycloak.realm(realmName).clients().findByClientId(clientId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Client not found: " + clientId + " in realm=" + realmName);
        }

        String internalClientId = found.get(0).getId();

        List<String> assignedRoles = keycloak.realm(realmName).users().get(userId)
                .roles().clientLevel(internalClientId).listAll()
                .stream().map(RoleRepresentation::getName).collect(Collectors.toList());

        log.info("Retrieved {} roles for userId={} clientId={} in realm={}", assignedRoles.size(), userId, clientId, realmName);

        return UserClientRolesInfo.builder()
                .userId(userId)
                .clientId(clientId)
                .assignedRoles(assignedRoles)
                .build();
    }

    private String generateRandomClientSecret() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder secret = new StringBuilder();
        for (int i = 0; i < 43; i++) {
            secret.append(chars.charAt(random.nextInt(chars.length())));
        }
        return secret.toString();
    }
}
