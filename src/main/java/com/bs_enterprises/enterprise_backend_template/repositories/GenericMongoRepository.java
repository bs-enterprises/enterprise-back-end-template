package com.bs_enterprises.enterprise_backend_template.repositories;

import com.bs_enterprises.enterprise_backend_template.repositories.impl.BaseMongoRepository;
import com.bs_enterprises.enterprise_backend_template.services.DatabaseService;
import com.bs_enterprises.enterprise_backend_template.services.IndexingService;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic concrete repository — supply entityClass + collectionName at construction time.
 */
@Slf4j
public class GenericMongoRepository<T> extends BaseMongoRepository<T> {

    private final Class<T> entityClass;
    private final String collectionName;

    public GenericMongoRepository(
            Class<T> entityClass,
            String collectionName,
            DatabaseService databaseService,
            IndexingService indexingService
    ) {
        super(databaseService, indexingService);
        this.entityClass = entityClass;
        this.collectionName = collectionName;
    }

    @Override
    protected Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    protected String getCollectionName() {
        return collectionName;
    }
}
