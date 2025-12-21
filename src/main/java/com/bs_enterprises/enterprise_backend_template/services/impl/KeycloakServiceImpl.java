package com.bs_enterprises.enterprise_backend_template.services.impl;

import com.bs_enterprises.enterprise_backend_template.config.KeycloakProperties;
import com.bs_enterprises.enterprise_backend_template.services.KeycloakService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.*;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

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
