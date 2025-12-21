package com.bs_enterprises.enterprise_backend_template.services;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IndexingService {

    void createIndexEntry(String tenant, MongoTemplate optionalMongoTemplate, String value, String collectionName);

    boolean isValueExists(String tenant, MongoTemplate optionalMongoTemplate, String value, String collectionName);

    void deleteIndexEntry(String tenant, MongoTemplate optionalMongoTemplate, String value, String collectionName);

    long countDocumentsInCollection(String tenant, MongoTemplate optionalMongoTemplate, String collectionName);

    boolean existsDocumentById(String tenant, MongoTemplate optionalMongoTemplate, String id, String collectionName);

    boolean existsAllDocumentsByIds(
            String tenant,
            MongoTemplate optionalMongoTemplate,
            List<String> ids,
            String collectionName
    );
}
