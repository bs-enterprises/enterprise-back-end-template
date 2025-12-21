package com.bs_enterprises.enterprise_backend_template.services.impl;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import com.bs_enterprises.enterprise_backend_template.models.users.UserSecrets;
import com.bs_enterprises.enterprise_backend_template.services.DatabaseService;
import com.bs_enterprises.enterprise_backend_template.services.UserSecretService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSecretServiceImpl implements UserSecretService {

    private final DatabaseService databaseService;

    @Override
    public UserSecrets save(UserSecrets userSecrets, String tenant, MongoTemplate optionalMongoTemplate) {
        MongoTemplate mongoTemplate = optionalMongoTemplate;
        if (optionalMongoTemplate == null) {
            mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);
        }
        try {
            UserSecrets saved = mongoTemplate.insert(userSecrets);
            log.info("🔐 UserSecret saved with id: {}", saved.getId());
            return saved;
        } catch (DuplicateKeyException e) {
            log.warn("⚠️ UserSecret with id '{}' already exists. Cannot insert duplicate.", userSecrets.getId());
            throw e;
        } catch (Exception e) {
            log.error("❌ Failed to save UserSecret: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public UserSecrets update(UserSecrets userSecrets, String tenant, MongoTemplate optionalMongoTemplate) {

        MongoTemplate mongoTemplate = optionalMongoTemplate;
        if (optionalMongoTemplate == null) {
            mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);
        }

        try {
            Query query = new Query(Criteria.where(MongoDBConstants.FIELD_ID).is(userSecrets.getId()));
            Update update = new Update();

            if (userSecrets.getKeycloakUserId() != null) {
                update.set("keycloakUserId", userSecrets.getKeycloakUserId());
            }

            if (update.getUpdateObject().isEmpty()) {
                log.warn("⚠️ No fields provided to update for UserSecret with id '{}'", userSecrets.getId());
                return mongoTemplate.findById(userSecrets.getId(), UserSecrets.class);
            }

            // Perform findAndModify to update and return the modified document
            FindAndModifyOptions options = FindAndModifyOptions.options()
                    .returnNew(true)      // Return the modified document
                    .upsert(false);       // Do not insert if not found

            UserSecrets updated = mongoTemplate.findAndModify(query, update, options, UserSecrets.class);

            if (updated == null) {
                log.warn("⚠️ No UserSecret found with id '{}' to update.", userSecrets.getId());
            } else {
                log.info("✅ UserSecret with id '{}' updated successfully.", updated.getId());
            }

            return updated;

        } catch (Exception e) {
            log.error("❌ Failed to update UserSecret with id '{}': {}", userSecrets.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public UserSecrets findById(String id, String tenant, MongoTemplate optionalMongoTemplate) {
        MongoTemplate mongoTemplate = optionalMongoTemplate;
        if (optionalMongoTemplate == null) {
            mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);
        }
        UserSecrets secret = mongoTemplate.findById(id, UserSecrets.class);
        if (secret != null) {
            log.info("🔍 Found UserSecret for id: {}", id);
        } else {
            log.warn("⚠️ No UserSecret found for id: {}", id);
        }
        return secret;
    }

    @Override
    public boolean deleteById(String id, String tenant, MongoTemplate optionalMongoTemplate) {
        MongoTemplate mongoTemplate = optionalMongoTemplate;
        if (optionalMongoTemplate == null) {
            mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);
        }
        Query query = Query.query(Criteria.where(MongoDBConstants.FIELD_ID).is(id));
        UserSecrets deleted = mongoTemplate.findAndRemove(query, UserSecrets.class);
        if (deleted != null) {
            log.info("🗑️ Deleted UserSecret with id: {}", id);
            return true;
        } else {
            log.warn("⚠️ No UserSecret found to delete for id: {}", id);
            return false;
        }
    }

}
