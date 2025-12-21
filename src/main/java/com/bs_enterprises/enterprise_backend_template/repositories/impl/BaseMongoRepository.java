package com.bs_enterprises.enterprise_backend_template.repositories.impl;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import com.bs_enterprises.enterprise_backend_template.keys.DatabaseKeys;
import com.bs_enterprises.enterprise_backend_template.repositories.BaseMongoRepositoryContract;
import com.bs_enterprises.enterprise_backend_template.services.DatabaseService;
import com.bs_enterprises.enterprise_backend_template.services.IndexingService;
import com.bs_enterprises.enterprise_backend_template.utils.QueryBuilderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseMongoRepository<T> implements BaseMongoRepositoryContract<T> {

    protected final DatabaseService databaseService;
    protected final IndexingService indexingService;

    /**
     * Concrete repo must provide entity class and collection name.
     */
    protected abstract Class<T> getEntityClass();

    protected abstract String getCollectionName();

    @Override
    public T create(T entity, String tenant) {
        log.info("create called — tenant='{}', entity='{}'", tenant, getEntityClass().getSimpleName());
        MongoTemplate mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);

        try {
            @SuppressWarnings("unchecked")
            T saved = (T) mongoTemplate.insert(entity, getCollectionName());
            log.info("create completed — tenant='{}', entity='{}'", tenant, getEntityClass().getSimpleName());
            return saved;
        } catch (Exception ex) {
            log.error("create failed — tenant='{}', entity='{}', error={}",
                    tenant, getEntityClass().getSimpleName(), ex.getMessage(), ex);
            throw new IllegalStateException(DatabaseKeys.CREATION_FAILED);
        }
    }

    /**
     * Update by id using Map of updates (validations are expected before calling).
     * Returns the updated document using findAndModify in one round-trip.
     */
    @Override
    public T update(String id, Map<String, Object> updates, String tenant) {
        log.info("update called — tenant='{}', id='{}', entity='{}', fields={}",
                tenant, id, getEntityClass().getSimpleName(), updates == null ? "{}" : updates.keySet());

        MongoTemplate mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);

        if (updates == null || updates.isEmpty()) {
            throw new IllegalStateException(DatabaseKeys.INVALID_UPDATE_PAYLOAD);
        }

        // existence check up-front
        if (!indexingService.existsDocumentById(tenant, mongoTemplate, id, getCollectionName())) {
            log.warn("update — tenant='{}', id='{}': not found in collection='{}'", tenant, id, getCollectionName());
            throw new IllegalStateException(DatabaseKeys.RECORD_NOT_FOUND);
        }

        Query query = new Query(Criteria.where(MongoDBConstants.FIELD_ID).is(id));
        Update update = new Update();
        updates.forEach(update::set);

        try {
            FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(false);
            T updated = mongoTemplate.findAndModify(query, update, options, getEntityClass(), getCollectionName());

            if (updated == null) {
                log.error("update — tenant='{}', id='{}': findAndModify returned null for collection='{}'",
                        tenant, id, getCollectionName());
                throw new IllegalStateException(DatabaseKeys.UPDATE_FAILED);
            }

            log.info("update completed — tenant='{}', id='{}', entity='{}'",
                    tenant, id, getEntityClass().getSimpleName());
            return updated;

        } catch (Exception ex) {
            log.error("update failed — tenant='{}', id='{}', entity='{}', error={}",
                    tenant, id, getEntityClass().getSimpleName(), ex.getMessage(), ex);
            throw new IllegalStateException(DatabaseKeys.UPDATE_FAILED);
        }
    }

    @Override
    public void delete(String id, String tenant) {
        log.info("delete called — tenant='{}', id='{}', entity='{}'", tenant, id, getEntityClass().getSimpleName());
        MongoTemplate mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);

        try {
            Query query = new Query(Criteria.where(MongoDBConstants.FIELD_ID).is(id));
            var result = mongoTemplate.remove(query, getEntityClass(), getCollectionName());

            if (result.getDeletedCount() == 0) {
                log.warn("delete — tenant='{}', id='{}': not found in collection='{}'", tenant, id, getCollectionName());
            }

            log.info("delete completed — tenant='{}', id='{}', entity='{}'",
                    tenant, id, getEntityClass().getSimpleName());
        } catch (Exception ex) {
            log.error("delete failed — tenant='{}', id='{}', entity='{}', error={}",
                    tenant, id, getEntityClass().getSimpleName(), ex.getMessage(), ex);
            throw new IllegalStateException(DatabaseKeys.DELETE_FAILED);
        }
    }

    @Override
    public T getById(String id, String tenant) {
        log.info("getById called — tenant='{}', id='{}', entity='{}'", tenant, id, getEntityClass().getSimpleName());
        MongoTemplate mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);

        T found = mongoTemplate.findById(id, getEntityClass(), getCollectionName());
        log.info("getById completed — tenant='{}', id='{}', entity='{}'", tenant, id, getEntityClass().getSimpleName());
        return found;
    }

    /**
     * Generic search using QueryBuilderUtil.buildQuery(searchParams).
     * Signature matches your desired pattern.
     */
    @Override
    public Page<T> search(Map<String, Object> searchParams, int page, int size, String tenant) {
        log.info("search called — tenant='{}', page={}, size={}, params={}",
                tenant, page, size, CollectionUtils.isEmpty(searchParams) ? "{}" : searchParams.keySet());

        MongoTemplate mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);

        try {
            Query query = QueryBuilderUtil.buildQuery(searchParams);

            long total = mongoTemplate.count(query, getEntityClass(), getCollectionName());

            Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
            query.with(pageable);
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

            List<T> items = mongoTemplate.find(query, getEntityClass(), getCollectionName());

            log.info("search completed — tenant='{}', totalMatches={}, returned={}, page={}, size={}",
                    tenant, total, (items == null ? 0 : items.size()), pageable.getPageNumber(), pageable.getPageSize());

            return new PageImpl<>(items, pageable, total);

        } catch (Exception ex) {
            log.error("search failed — tenant='{}', error={}", tenant, ex.getMessage(), ex);
            throw new IllegalStateException(DatabaseKeys.INVALID_QUERY_PARAMETERS);
        }
    }


    @Override
    public long bulkDeleteByIds(List<String> ids, String tenant) {
        log.info("bulkDeleteByIds called — tenant='{}', entity='{}', idsCount={}",
                tenant, getEntityClass().getSimpleName(), (ids == null ? 0 : ids.size()));

        if (CollectionUtils.isEmpty(ids)) {
            log.warn("bulkDeleteByIds — tenant='{}': empty ids provided for collection='{}'", tenant, getCollectionName());
            return 0L;
        }

        MongoTemplate mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);

        try {
            Query query = new Query(Criteria.where(MongoDBConstants.FIELD_ID).in(ids));
            var result = mongoTemplate.remove(query, getEntityClass(), getCollectionName());
            long deleted = result.getDeletedCount();

            log.info("bulkDeleteByIds completed — tenant='{}', collection='{}', requested={}, deleted={}",
                    tenant, getCollectionName(), ids.size(), deleted);
            return deleted;
        } catch (Exception ex) {
            log.error("bulkDeleteByIds failed — tenant='{}', collection='{}', error={}",
                    tenant, getCollectionName(), ex.getMessage(), ex);
            throw new IllegalStateException(DatabaseKeys.DELETE_FAILED);
        }
    }

    @Override
    public long bulkDeleteByFilters(Map<String, Object> filters, String tenant) {
        log.info("bulkDeleteByFilters called — tenant='{}', entity='{}', filterKeys={}",
                tenant, getEntityClass().getSimpleName(), CollectionUtils.isEmpty(filters) ? "{}" : filters.keySet());

        if (CollectionUtils.isEmpty(filters)) {
            log.warn("bulkDeleteByFilters — tenant='{}': empty filters provided for collection='{}'", tenant, getCollectionName());
            throw new IllegalStateException(DatabaseKeys.INVALID_QUERY_PARAMETERS);
        }

        MongoTemplate mongoTemplate = databaseService.changeDatabaseAndGetNewMongoTemplate(tenant);

        try {
            Query query = QueryBuilderUtil.buildQuery(filters);
            var result = mongoTemplate.remove(query, getEntityClass(), getCollectionName());
            long deleted = result.getDeletedCount();

            log.info("bulkDeleteByFilters completed — tenant='{}', collection='{}', deleted={}",
                    tenant, getCollectionName(), deleted);
            return deleted;
        } catch (Exception ex) {
            log.error("bulkDeleteByFilters failed — tenant='{}', collection='{}', error={}",
                    tenant, getCollectionName(), ex.getMessage(), ex);
            throw new IllegalStateException(DatabaseKeys.DELETE_FAILED);
        }
    }

}
