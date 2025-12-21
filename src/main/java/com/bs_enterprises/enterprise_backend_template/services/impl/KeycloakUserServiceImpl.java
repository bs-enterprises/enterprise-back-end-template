package com.bs_enterprises.enterprise_backend_template.services.impl;

import com.bs_enterprises.enterprise_backend_template.models.users.KeycloakUserModel;
import com.bs_enterprises.enterprise_backend_template.services.KeycloakUserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of KeycloakUserService using the Keycloak admin client.
 * Uses a central admin Keycloak client (master/admin). studioIds are optional and stored
 * as an attribute named "studioIds" (list).
 * <p>
 * Note: realmName is considered the tenant identifier (different tenants are different realms).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final Keycloak keycloak; // injected master/admin Keycloak client


    // attribute name in Keycloak used to store list of studio ids
    private final ObjectMapper objectMapper;

    // ----------------------------
    // Helper: convert model -> Keycloak UserRepresentation
    // studioId removed — we only include studioIds if present on the model.
    // ----------------------------
    private UserRepresentation toRepresentation(KeycloakUserModel model) {
        UserRepresentation user = new UserRepresentation();

        // Keep behaviour predictable — set username and enabled always (username is required)
        if (model.getUsername() != null) {
            user.setUsername(model.getUsername());
        }

        user.setEnabled(model.isEnabled());
        user.setEmailVerified(model.isEmailVerified());

        if (model.getEmail() != null) {
            user.setEmail(model.getEmail());
        }
        if (model.getFirstName() != null) {
            user.setFirstName(model.getFirstName());
        }
        if (model.getLastName() != null) {
            user.setLastName(model.getLastName());
        }

        // Build attributes map only when we have attributes to set.
        Map<String, List<String>> attrs = new HashMap<>();

        // Only set attributes when non-empty to avoid sending empty attribute maps to Keycloak
        if (!attrs.isEmpty()) {
            user.setAttributes(attrs);
        }

        // If the model has an id (we store memberId as KeycloakUserModel.id in your flows),
        // set it on the representation so client libraries that expect it can use it.
        if (model.getId() != null) {
            user.setId(model.getId());
        }

        return user;
    }


    @Override
    public String createUser(String realmName, KeycloakUserModel model) {
        Objects.requireNonNull(realmName, "realmName required");
        Objects.requireNonNull(model, "model required");

        UserRepresentation user = toRepresentation(model);

        try (Response response = keycloak.realm(realmName).users().create(user)) {
            int status = response.getStatus();

            // ---------------------------------------
            // SUCCESS → 201 or 204
            // ---------------------------------------
            if (status == 201 || status == 204) {
                // Try to extract ID from Location header
                String location = response.getLocation() != null ? response.getLocation().toString() : null;

                if (location != null) {
                    String id = location.substring(location.lastIndexOf('/') + 1);
                    log.info("Created user '{}' in realm '{}' (id={})",
                            model.getUsername(), realmName, id);
                    return id;
                }

                // No location? → Use fallback search
                return fallbackFetchCreatedId(realmName, model.getUsername());
            }

            // ---------------------------------------
            // FAILURE → read body for debugging
            // ---------------------------------------
            String body = null;
            try {
                body = response.readEntity(String.class);
            } catch (Exception e) {
                log.warn("Unable to read Keycloak response body while creating user: {}", e.getMessage());
            }

            // Parse JSON if possible
            String message = extractErrorMessage(body);
            String field = extractField(body);

            String detail = (message != null ? message : (body != null ? body : "No error details"));

            // ---------------------------------------
            // Error mapping
            // ---------------------------------------
            if (status == 409) {
                String msg = "Conflict creating user (username=" + model.getUsername() + "): " + detail;
                if (field != null) msg += " (field: " + field + ")";
                throw new IllegalArgumentException(msg);
            }

            if (status == 400) {
                throw new IllegalArgumentException(
                        "Bad request while creating user (username=" + model.getUsername() +
                                "): " + detail
                );
            }

            if (status == 401 || status == 403) {
                throw new IllegalStateException(
                        "Unauthorized/Forbidden creating user: HTTP " + status +
                                " - " + detail
                );
            }

            // Generic fallback
            throw new IllegalStateException(
                    "Failed to create user (username=" + model.getUsername() +
                            "). HTTP " + status + " - " + detail
            );

        }
    }

    private String fallbackFetchCreatedId(String realmName, String username) {
        List<UserRepresentation> found = findByUsername(realmName, username, 0, 5);

        if (!found.isEmpty()) {
            String id = found.get(0).getId();
            log.info("Fallback match: Keycloak user '{}' found (id={})", username, id);
            return id;
        }

        throw new IllegalStateException(
                "User creation succeeded but could not determine user ID " +
                        "(no Location header and search fallback failed)"
        );
    }

    private String extractErrorMessage(String body) {
        if (body == null) return null;
        try {
            JsonNode node = objectMapper.readTree(body);

            if (node.has("errorMessage")) return node.get("errorMessage").asText();
            if (node.has("error")) return node.get("error").asText();
            return null;
        } catch (Exception ignore) {
            return null; // body may not be JSON
        }
    }

    private String extractField(String body) {
        if (body == null) return null;
        try {
            JsonNode node = objectMapper.readTree(body);

            if (node.has("field")) return node.get("field").asText();
            return null;
        } catch (Exception ignore) {
            return null;
        }
    }


    // ----------------------------
    // Get user (no studio check by default)
    // ----------------------------
    @Override
    public UserRepresentation getUser(String realmName, String ignoredStudio, String keycloakUserId) {
        Objects.requireNonNull(realmName, "realmName required");
        Objects.requireNonNull(keycloakUserId, "keycloakUserId required");

        try {
            UserRepresentation user = keycloak.realm(realmName).users().get(keycloakUserId).toRepresentation();
            return user;
        } catch (Exception e) {
            log.error("Failed to fetch Keycloak user (realm={}, id={}): {}", realmName, keycloakUserId, e.getMessage(), e);
            throw new IllegalStateException("User not found: " + keycloakUserId, e);
        }
    }

    // ----------------------------
    // Update user (merge with existing via UserResource to avoid BadRequest)
    // ----------------------------
    @Override
    public void updateUser(String realmName, String keycloakUserId, KeycloakUserModel model) {
        Objects.requireNonNull(realmName, "realmName required");
        Objects.requireNonNull(keycloakUserId, "keycloakUserId required");
        Objects.requireNonNull(model, "model required");

        try {
            UserResource userResource = keycloak.realm(realmName).users().get(keycloakUserId);

            // 1) Get existing Keycloak user to read current state/attributes
            UserRepresentation existing;
            try {
                existing = userResource.toRepresentation();
            } catch (Exception e) {
                log.error("Failed fetching Keycloak user for update: {}", e.getMessage(), e);
                throw new IllegalStateException("Unable to fetch existing Keycloak user: " + keycloakUserId, e);
            }

            // 2) Validate Username (Cannot change)
            if (model.getUsername() != null &&
                    !model.getUsername().equals(existing.getUsername())) {
                throw new IllegalArgumentException(
                        "Username cannot be changed in Keycloak. Existing="
                                + existing.getUsername()
                                + ", attempted=" + model.getUsername()
                );
            }

            // --------------------------------------------------------------------
            // FIX: Create a CLEAN representation for the update
            // --------------------------------------------------------------------
            UserRepresentation updateRep = new UserRepresentation();
            updateRep.setId(existing.getId()); // ID is required for update context
            updateRep.setUsername(existing.getUsername()); // Username is required

            // 3) Update fields
            // Use model value if present, otherwise fall back to existing value
            // (or just set what is in the model if you want partial updates).

            updateRep.setFirstName(model.getFirstName() != null ? model.getFirstName() : existing.getFirstName());
            updateRep.setLastName(model.getLastName() != null ? model.getLastName() : existing.getLastName());
            updateRep.setEmail(model.getEmail() != null ? model.getEmail() : existing.getEmail());

            // Booleans: in your model these are primitives, so they are always true/false.
            // If you want to only update if changed, you'd need Boolean wrapper in DTO.
            // For now, we apply the model's state:
            updateRep.setEnabled(model.isEnabled());
            updateRep.setEmailVerified(model.isEmailVerified());

            // 4) Handle Attributes
            Map<String, List<String>> attrs =
                    existing.getAttributes() == null ? new HashMap<>() : new HashMap<>(existing.getAttributes());


            // Set the attributes on the CLEAN object
            // Keycloak hates empty maps, pass null if empty
            if (attrs.isEmpty()) {
                updateRep.setAttributes(null);
            } else {
                updateRep.setAttributes(attrs);
            }

            // 5) Execute Update
            try {
                // We send 'updateRep' (the clean object), NOT 'existing'
                userResource.update(updateRep);
                log.info("Updated Keycloak user {} in realm {}", keycloakUserId, realmName);
            } catch (jakarta.ws.rs.BadRequestException bre) {
                String body = null;
                try {
                    if (bre.getResponse() != null) body = bre.getResponse().readEntity(String.class);
                } catch (Exception ignore) {
                }

                log.error("Keycloak 400 during update. Body={}", body);
                throw new IllegalStateException("Keycloak rejected update: " + body, bre);
            }

        } catch (Exception ex) {
            log.error("Failed to update Keycloak user (realm={}, id={}): {}", realmName, keycloakUserId, ex.getMessage(), ex);
            throw ex;
        }
    }

    // ----------------------------
    // Delete user (no studio check by default)
    // ----------------------------
    @Override
    public void deleteUser(String realmName, String userId) {
        Objects.requireNonNull(realmName, "realmName required");
        Objects.requireNonNull(userId, "userId required");

        try {
            // Attempt delete; Keycloak will respond accordingly if user not found
            keycloak.realm(realmName).users().get(userId).remove();
            log.info("Deleted user (id={}) from realm '{}'", userId, realmName);
        } catch (Exception ex) {
            log.error("Failed to delete Keycloak user (realm={}, id={}): {}", realmName, userId, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to delete Keycloak user: " + userId, ex);
        }
    }

    // ----------------------------
    // Search users (no studio filter enforced at API level; caller can filter using extractStudioIds())
    // ----------------------------
    @Override
    public List<UserRepresentation> searchUsers(String realmName, String search, int first, int max) {
        Objects.requireNonNull(realmName, "realmName required");

        List<UserRepresentation> results = keycloak.realm(realmName).users().search(search == null ? "" : search, first, max);
        if (results == null || results.isEmpty()) return Collections.emptyList();
        return results;
    }

    @Override
    public List<UserRepresentation> findByUsername(String realmName, String username, int first, int max) {
        Objects.requireNonNull(realmName, "realmName required");
        List<UserRepresentation> results = keycloak.realm(realmName).users().search(username, first, max);
        if (results == null || results.isEmpty()) return Collections.emptyList();
        return results;
    }

    // ----------------------------
    // Set password
    // ----------------------------
    @Override
    public void setPassword(String realmName, String ignoredStudio, String userId, String password, boolean temporary) {
        Objects.requireNonNull(realmName, "realmName required");
        Objects.requireNonNull(userId, "userId required");
        Objects.requireNonNull(password, "password required");

        try {
            // Create credential
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(temporary);

            // Reset password using UserResource
            keycloak.realm(realmName).users().get(userId).resetPassword(credential);
            log.info("Password set for user (id={}) in realm '{}'", userId, realmName);
        } catch (Exception ex) {
            log.error("Failed to set password for user (realm={}, id={}): {}", realmName, userId, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to set password for Keycloak user: " + userId, ex);
        }
    }
}
