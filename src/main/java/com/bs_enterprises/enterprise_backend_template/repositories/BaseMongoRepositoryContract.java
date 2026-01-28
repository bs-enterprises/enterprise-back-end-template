package com.bs_enterprises.enterprise_backend_template.repositories;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BaseMongoRepositoryContract<T> {

    T create(T entity, String tenant);

    T update(String id, Map<String, Object> updates, List<String> allowedKeysForUpdate, String tenant);

    long bulkUpdateByFilters(
            Map<String, Object> filters,
            Map<String, Object> updates,
            List<String> allowedKeysForUpdate,
            String tenant
    );

    void delete(String id, String tenant);

    T getById(String id, String tenant);

    Page<T> search(Map<String, Object> searchParams, int page, int size, String tenant);

    long bulkDeleteByIds(List<String> ids, String tenant);

    long bulkDeleteByFilters(Map<String, Object> filters, String tenant);
}
