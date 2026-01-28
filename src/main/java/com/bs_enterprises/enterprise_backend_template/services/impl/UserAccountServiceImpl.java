package com.bs_enterprises.enterprise_backend_template.services.impl;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import com.bs_enterprises.enterprise_backend_template.keys.DatabaseKeys;
import com.bs_enterprises.enterprise_backend_template.keys.ExecutionKeys;
import com.bs_enterprises.enterprise_backend_template.models.users.KeycloakUserModel;
import com.bs_enterprises.enterprise_backend_template.models.users.LoadedArtifacts;
import com.bs_enterprises.enterprise_backend_template.models.users.UserSecrets;
import com.bs_enterprises.enterprise_backend_template.repositories.GenericMongoRepository;
import com.bs_enterprises.enterprise_backend_template.services.IndexingService;
import com.bs_enterprises.enterprise_backend_template.services.KeycloakUserService;
import com.bs_enterprises.enterprise_backend_template.services.UserAccountService;
import com.bs_enterprises.enterprise_backend_template.services.UserSecretService;
import com.bs_enterprises.enterprise_backend_template.utils.SnowflakeIdGeneratorUtil;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {

    private final KeycloakUserService keycloakUserService;
    private final IndexingService indexingService;
    private final UserSecretService userSecretService;
    private final GenericMongoRepository<KeycloakUserModel> keycloakUserRepository;

    @Override
    public String prepareAndValidateIdentifiers(String realmName,
                                                String userId,
                                                String email,
                                                String phone,
                                                List<String> studioIds,
                                                boolean studioRequired) {
        if (realmName == null || realmName.isBlank()) throw new IllegalArgumentException(ExecutionKeys.REALM_REQUIRED);

        String id = userId;
        if (id == null || id.isBlank()) {
            id = String.valueOf(SnowflakeIdGeneratorUtil.generateId());
            log.info("Generated id='{}' for realm='{}'", id, realmName);
        }

        // uid uniqueness
        if (indexingService.isValueExists(realmName, null, id, MongoDBConstants.INDEX_UIDS)) {
            log.warn("{} — realm='{}', id='{}'", ExecutionKeys.USER_ID_ALREADY_EXISTS, realmName, id);
            throw new IllegalArgumentException(ExecutionKeys.USER_ID_ALREADY_EXISTS);
        }

        // email uniqueness
        if (!StringUtils.isBlank(email)) {
            if (indexingService.isValueExists(realmName, null, email, MongoDBConstants.INDEX_EMAILS)) {
                log.warn("{} — realm='{}', email='{}'", ExecutionKeys.EMAIL_ALREADY_EXISTS, realmName, email);
                throw new IllegalArgumentException(ExecutionKeys.EMAIL_ALREADY_EXISTS);
            }
        }

        // phone uniqueness
        if (!StringUtils.isBlank(phone)) {
            if (indexingService.isValueExists(realmName, null, phone, MongoDBConstants.INDEX_MOBILES)) {
                log.warn("{} — realm='{}', phone='{}'", ExecutionKeys.PHONE_ALREADY_EXISTS, realmName, phone);
                throw new IllegalArgumentException(ExecutionKeys.PHONE_ALREADY_EXISTS);
            }
        }

        // validate studios if required or if provided
        if ((studioRequired || (studioIds != null && !studioIds.isEmpty()))
                && !indexingService.existsAllDocumentsByIds(realmName, null, studioIds, MongoDBConstants.COLLECTION_STUDIOS)) {
            log.warn("{} — realm='{}', studioIds='{}'", ExecutionKeys.STUDIO_NOT_FOUND, realmName, studioIds);
            throw new IllegalArgumentException(ExecutionKeys.STUDIO_NOT_FOUND);
        }

        return id;
    }

    @Override
    public void provisionNewUser(String realmName, KeycloakUserModel kcUser) {
        Objects.requireNonNull(kcUser, "kcUser required");

        // create in Keycloak
        log.info("Creating user in Keycloak realm='{}' username='{}'", realmName, kcUser.getUsername());
        String keycloakUserId = keycloakUserService.createUser(realmName, kcUser);
        log.info("Keycloak created id='{}' for local id='{}'", keycloakUserId, kcUser.getId());

        // persist KC model via repository
        keycloakUserRepository.create(kcUser, realmName);

        // persist secrets
        UserSecrets secrets = new UserSecrets(kcUser.getId(), keycloakUserId);
        userSecretService.save(secrets, realmName, null);

        // create indices: uid, email, phone
        indexingService.createIndexEntry(realmName, null, kcUser.getId(), MongoDBConstants.INDEX_UIDS);
        if (!StringUtils.isBlank(kcUser.getEmail()))
            indexingService.createIndexEntry(realmName, null, kcUser.getEmail(), MongoDBConstants.INDEX_EMAILS);
        if (!StringUtils.isBlank(kcUser.getPhone()))
            indexingService.createIndexEntry(realmName, null, kcUser.getPhone(), MongoDBConstants.INDEX_MOBILES);
    }

    @Override
    public LoadedArtifacts loadArtifacts(String realmName, String userId) {
        Objects.requireNonNull(realmName);
        Objects.requireNonNull(userId);

        UserSecrets secrets = userSecretService.findById(userId, realmName, null);
        if (secrets == null)
            throw new IllegalStateException(DatabaseKeys.RECORD_NOT_FOUND + ": UserSecrets not found for id: " + userId);

        KeycloakUserModel kcUser = keycloakUserRepository.getById(userId, realmName);
        if (kcUser == null)
            throw new IllegalStateException(DatabaseKeys.RECORD_NOT_FOUND + ": KeycloakUserModel not found for id: " + userId);

        return new LoadedArtifacts(kcUser, secrets);
    }

    @Override
    public KeycloakUserModel updateUser(String realmName,
                                        String userId,
                                        Map<String, Object> updates,
                                        List<String> allowedKeys) {

        Objects.requireNonNull(userId);
        Objects.requireNonNull(updates);
        // allowedKeys parameter is ignored on purpose — we only accept fields defined on KeycloakUserModel

        // load artifacts
        UserSecrets secrets = userSecretService.findById(userId, realmName, null);
        if (secrets == null) {
            log.info("UserSecrets not found for id='{}' in realm='{}'", userId, realmName);
            throw new IllegalStateException(DatabaseKeys.RECORD_NOT_FOUND);
        }

        KeycloakUserModel kcUser = keycloakUserRepository.getById(userId, realmName);
        if (kcUser == null) {
            log.info("KeycloakUserModel not found for id='{}' in realm='{}'", userId, realmName);
            throw new IllegalStateException(DatabaseKeys.RECORD_NOT_FOUND);
        }

        // track old unique values
        String oldEmail = kcUser.getEmail();
        String oldPhone = kcUser.getPhone();

        // Prepare kcUpdate map for repository update (findAndModify)
        Map<String, Object> kcUpdates = new HashMap<>();

        for (Map.Entry<String, Object> e : updates.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();

            // Only accept fields that exist on KeycloakUserModel
            switch (k) {
                case "username" -> {
                    String newUsername = v == null ? null : String.valueOf(v).trim();
                    if (!Objects.equals(newUsername, kcUser.getUsername())) {
                        kcUser.setUsername(newUsername);
                        kcUpdates.put("username", newUsername);
                    }
                }

                case "firstName" -> {
                    String newFirst = v == null ? null : String.valueOf(v);
                    if (!Objects.equals(newFirst, kcUser.getFirstName())) {
                        kcUser.setFirstName(newFirst);
                        kcUpdates.put("firstName", newFirst);
                    }
                }

                case "lastName" -> {
                    String newLast = v == null ? null : String.valueOf(v);
                    if (!Objects.equals(newLast, kcUser.getLastName())) {
                        kcUser.setLastName(newLast);
                        kcUpdates.put("lastName", newLast);
                    }
                }

                case "email" -> {
                    String newEmail = v == null ? null : String.valueOf(v).trim();
                    if (!Objects.equals(newEmail, oldEmail)) {
                        kcUser.setEmail(newEmail);
                        kcUpdates.put("email", newEmail);
                    }
                }

                case "phone" -> {
                    String newPhone = v == null ? null : String.valueOf(v).trim();
                    if (!Objects.equals(newPhone, oldPhone)) {
                        kcUser.setPhone(newPhone);
                        kcUpdates.put("phone", newPhone);
                    }
                }

                case "enabled" -> {
                    Boolean newEnabled;
                    if (v instanceof Boolean b) newEnabled = b;
                    else newEnabled = Boolean.valueOf(String.valueOf(v));
                    if (newEnabled != kcUser.isEnabled()) {
                        kcUser.setEnabled(newEnabled);
                        kcUpdates.put("enabled", newEnabled);
                    }
                }

                case "emailVerified" -> {
                    Boolean newEmailVerified;
                    if (v instanceof Boolean b) newEmailVerified = b;
                    else newEmailVerified = Boolean.valueOf(String.valueOf(v));
                    if (newEmailVerified != kcUser.isEmailVerified()) {
                        kcUser.setEmailVerified(newEmailVerified);
                        kcUpdates.put("emailVerified", newEmailVerified);
                    }
                }

                case "studioIds" -> {
                    if (v instanceof Collection<?> collection) {
                        // validate that all studio ids exist
                        @SuppressWarnings("unchecked")
                        Collection<String> ids = (Collection<String>) collection;
                        if (!ids.isEmpty() && !indexingService.existsAllDocumentsByIds(realmName, null, List.copyOf(ids), MongoDBConstants.COLLECTION_STUDIOS)) {
                            log.warn("updateUser — tenant='{}', id='{}': some studioIds not found {}", realmName, userId, ids);
                            throw new IllegalArgumentException(ExecutionKeys.STUDIO_NOT_FOUND);
                        }
                        kcUpdates.put("studioIds", List.copyOf(ids));
                    } else {
                        log.warn("updateUser — tenant='{}', id='{}': studioIds must be a collection, got={}", realmName, userId,
                                v == null ? "null" : v.getClass().getName());
                        throw new IllegalStateException(DatabaseKeys.INVALID_UPDATE_PAYLOAD);
                    }
                }

                default -> {
                    // ignore unknown fields silently but log
                    log.warn("updateUser — tenant='{}', id='{}': ignoring unsupported field '{}'", realmName, userId, k);
                }
            }
        }

        // update Keycloak (use stored keycloak id)
        String keycloakUserId = secrets.getKeycloakUserId();
        try {
            keycloakUserService.updateUser(realmName, keycloakUserId, kcUser);
        } catch (Exception ex) {
            log.error("Failed to update Keycloak user for id={}: {}", userId, ex.getMessage(), ex);
            throw new IllegalStateException(DatabaseKeys.UPDATE_FAILED);
        }

        // update indices for email/phone
        String newEmail = kcUser.getEmail();
        String newPhone = kcUser.getPhone();

        if (!Objects.equals(oldEmail, newEmail)) {
            if (oldEmail != null && !oldEmail.isBlank())
                indexingService.deleteIndexEntry(realmName, null, oldEmail, MongoDBConstants.INDEX_EMAILS);
            if (newEmail != null && !newEmail.isBlank())
                indexingService.createIndexEntry(realmName, null, newEmail, MongoDBConstants.INDEX_EMAILS);
        }

        if (!Objects.equals(oldPhone, newPhone)) {
            if (oldPhone != null && !oldPhone.isBlank())
                indexingService.deleteIndexEntry(realmName, null, oldPhone, MongoDBConstants.INDEX_MOBILES);
            if (newPhone != null && !newPhone.isBlank())
                indexingService.createIndexEntry(realmName, null, newPhone, MongoDBConstants.INDEX_MOBILES);
        }

        // persist changes to KeycloakUserModel via repository update if kcUpdates present
        if (!kcUpdates.isEmpty()) {
            keycloakUserRepository.update(userId, kcUpdates,KeycloakUserModel.allowedKeysForUpdate , realmName);
        }

        // return fresh model
        return keycloakUserRepository.getById(userId, realmName);
    }

    @Override
    public void deleteUser(String realmName, String userId) {
        Objects.requireNonNull(userId);
        UserSecrets secrets = userSecretService.findById(userId, realmName, null);
        if (secrets == null) {
            log.warn("UserSecrets not found for id='{}' in realm='{}'", userId, realmName);
            throw new IllegalStateException(ExecutionKeys.USER_NOT_FOUND);
        }
        String keycloakUserId = secrets.getKeycloakUserId();

        // attempt delete in Keycloak
        try {
            keycloakUserService.deleteUser(realmName, keycloakUserId);
        } catch (Exception ex) {
            log.error("Failed to delete user in Keycloak for keycloakId={}: {}", keycloakUserId, ex.getMessage(), ex);
            throw new IllegalStateException(DatabaseKeys.DELETE_FAILED);
        }

        // fetch kcUser to know email/phone
        KeycloakUserModel kcUser = null;
        try {
            kcUser = keycloakUserRepository.getById(userId, realmName);
        } catch (Exception ignored) { /* may be missing */ }

        // delete indices (guarded)
        try {
            indexingService.deleteIndexEntry(realmName, null, userId, MongoDBConstants.INDEX_UIDS);
            if (kcUser != null && kcUser.getEmail() != null)
                indexingService.deleteIndexEntry(realmName, null, kcUser.getEmail(), MongoDBConstants.INDEX_EMAILS);
            if (kcUser != null && kcUser.getPhone() != null)
                indexingService.deleteIndexEntry(realmName, null, kcUser.getPhone(), MongoDBConstants.INDEX_MOBILES);
        } catch (Exception ex) {
            log.warn("Failed to delete indices for id={}: {}", userId, ex.getMessage(), ex);
        }

        // delete documents via repository (ignore not-found)
        try {
            keycloakUserRepository.delete(userId, realmName);
        } catch (Exception ignored) { /* ignore */ }

        boolean secretDeleted = userSecretService.deleteById(userId, realmName, null);
        if (!secretDeleted) log.warn("UserSecrets deletion returned false for id='{}' realm='{}'", userId, realmName);
    }
}
