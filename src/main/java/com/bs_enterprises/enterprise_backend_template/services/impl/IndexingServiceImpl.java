package com.bs_enterprises.enterprise_backend_template.services.impl;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import com.bs_enterprises.enterprise_backend_template.models.users.IndexEntry;
import com.bs_enterprises.enterprise_backend_template.services.DatabaseService;
import com.bs_enterprises.enterprise_backend_template.services.IndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {


    private final DatabaseService databaseService;

    public void createIndexEntry(String tenant, MongoTemplate optionalMongoTemplate, String value, String collectionName) {
        MongoTemplate mongoTemplate = getMongoTemplate(tenant, optionalMongoTemplate);
        IndexEntry indexEntry = new IndexEntry(value);
        try {
            mongoTemplate.insert(indexEntry, collectionName);
            log.info("Index entry created for value: {} in collection: {}", value, collectionName);
        } catch (DuplicateKeyException ex) {
            log.warn("Duplicate index entry found for value: {} in collection: {}", value, collectionName);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to create index entry for value: {} in collection: {}. Error: {}", value, collectionName,
                    ex.getMessage());
            throw ex;
        }
    }

    private MongoTemplate getMongoTemplate(String tenant, MongoTemplate optionalMongoTemplate) {
        MongoTemplate mongoTemplate = optionalMongoTemplate;
        if (mongoTemplate == null) {
            mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);
        }
        return mongoTemplate;
    }

    public boolean isValueExists(String tenant, MongoTemplate optionalMongoTemplate, String value, String collectionName) {
        MongoTemplate mongoTemplate = getMongoTemplate(tenant, optionalMongoTemplate);
        IndexEntry entry = mongoTemplate.findById(value, IndexEntry.class, collectionName);
        boolean exists = entry != null;
        log.info("Checked existence of value: {} in collection: {} - Exists: {}", value, collectionName, exists);
        return exists;
    }

    public void deleteIndexEntry(String tenant, MongoTemplate optionalMongoTemplate, String value, String collectionName) {
        MongoTemplate mongoTemplate = getMongoTemplate(tenant, optionalMongoTemplate);
        IndexEntry deleted = mongoTemplate.findAndRemove(
                Query.query(
                        Criteria.where(MongoDBConstants.FIELD_ID).is(value)
                ),
                IndexEntry.class,
                collectionName
        );
        boolean success = deleted != null;
        log.info("Deleted index entry for value: {} in collection: {} - Success: {}", value, collectionName, success);
    }

    public long countDocumentsInCollection(String tenant, MongoTemplate optionalMongoTemplate, String collectionName) {
        MongoTemplate mongoTemplate = getMongoTemplate(tenant, optionalMongoTemplate);
        long count = mongoTemplate.getCollection(collectionName).countDocuments();
        log.info("Total documents in collection '{}': {}", collectionName, count);
        return count;
    }

    @Override
    public boolean existsDocumentById(String tenant, MongoTemplate optionalMongoTemplate, String id, String collectionName) {
        MongoTemplate mongoTemplate = getMongoTemplate(tenant, optionalMongoTemplate);

        Object result = mongoTemplate.findById(id, Object.class, collectionName);
        boolean exists = (result != null);

        log.info("Checked existence of document with id={} in collection='{}' → Exists: {}",
                id, collectionName, exists);

        return exists;
    }

    @Override
    public boolean existsAllDocumentsByIds(
            String tenant,
            MongoTemplate optionalMongoTemplate,
            List<String> ids,
            String collectionName
    ) {
        MongoTemplate mongoTemplate = getMongoTemplate(tenant, optionalMongoTemplate);

        if (ids == null || ids.isEmpty()) {
            log.warn("existsAllDocumentsByIds called with empty/null id list for collection='{}'", collectionName);
            return true;
        }

        Query query = new Query(Criteria.where(MongoDBConstants.FIELD_ID).in(ids));
        long foundCount = mongoTemplate.count(query, collectionName);

        boolean allExist = (foundCount == ids.size());

        log.info("Checked existence of {} document ids in collection='{}' → Found: {}, AllExist: {}",
                ids.size(), collectionName, foundCount, allExist);

        return allExist;
    }


}
