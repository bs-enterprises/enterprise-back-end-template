package com.bs_enterprises.enterprise_backend_template.repositories;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Generic CRUD service contract for Mongo-backed entities
 *
 * @param <E> Entity type
 */
public interface BaseCrudService<E> {

    E create(E entity, String tenant);

    /**
     * Create an entity in a specified collection
     * Allows saving the entity to a different collection than the default
     *
     * @param entity the entity to create
     * @param tenant the tenant/realm
     * @param collectionName the specific collection name to save to
     * @return the created entity
     */
    E create(E entity, String tenant, String collectionName);

    E update(String id, Map<String, Object> updates, String tenant);

    long bulkUpdateByFilters(
            Map<String, Object> filters,
            Map<String, Object> updates,
            String tenant
    );

    void delete(String id, String tenant);

    E getById(String id, String tenant);

    Page<E> search(
            Map<String, Object> searchParams,
            int page,
            int size,
            String tenant
    );

    long bulkDeleteByIds(List<String> ids, String tenant);

    long bulkDeleteByFilters(Map<String, Object> filters, String tenant);

    long countByFilters(Map<String, Object> filters, String tenant);
}
