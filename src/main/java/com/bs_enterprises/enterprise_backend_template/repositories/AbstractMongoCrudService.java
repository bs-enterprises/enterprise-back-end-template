package com.bs_enterprises.enterprise_backend_template.repositories;

import com.bs_enterprises.enterprise_backend_template.utils.DateUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractMongoCrudService<E>
        implements BaseCrudService<E> {

    protected final GenericMongoRepository<E> repository;

    protected AbstractMongoCrudService(GenericMongoRepository<E> repository) {
        this.repository = repository;
    }

    protected abstract List<String> allowedKeysForUpdate();

    protected List<String> getFieldsRequiringInstantConversion() {
        return List.of();
    }

    protected Map<String, Object> cleanUpdates(Map<String, Object> updates) {
        Map<String, Object> cleanedUpdates = new HashMap<>();
        List<String> allowed = allowedKeysForUpdate();
        List<String> instantFields = getFieldsRequiringInstantConversion();

        for (String key : allowed) {
            if (!updates.containsKey(key)) {
                continue;
            }

            Object value = updates.get(key);

            if (instantFields.contains(key)) {
                cleanedUpdates.put(key, DateUtilities.convertToInstant(value));
            } else {
                cleanedUpdates.put(key, value);
            }
        }

        return cleanedUpdates;
    }


    @Override
    public E create(E entity, String tenant) {
        return repository.create(entity, tenant);
    }

    @Override
    public E create(E entity, String tenant, String collectionName) {
        return ((BaseMongoRepositoryContract<E>) repository).create(entity, tenant, collectionName);
    }

    @Override
    public E update(String id, Map<String, Object> updates, String tenant) {
        Map<String, Object> cleanedUpdates = cleanUpdates(updates);
        return repository.update(id, cleanedUpdates, tenant);
    }

    @Override
    public long bulkUpdateByFilters(
            Map<String, Object> filters,
            Map<String, Object> updates,
            String tenant
    ) {
        Map<String, Object> cleanedUpdates = cleanUpdates(updates);
        return repository.bulkUpdateByFilters(
                filters, cleanedUpdates, tenant
        );
    }

    @Override
    public void delete(String id, String tenant) {
        repository.delete(id, tenant);
    }

    @Override
    public E getById(String id, String tenant) {
        if (id == null || tenant == null) return null;
        return repository.getById(id, tenant);
    }

    @Override
    public Page<E> search(
            Map<String, Object> searchParams,
            int page,
            int size,
            String tenant
    ) {
        return repository.search(searchParams, page, size, tenant);
    }

    @Override
    public long bulkDeleteByIds(List<String> ids, String tenant) {
        return repository.bulkDeleteByIds(ids, tenant);
    }

    @Override
    public long bulkDeleteByFilters(
            Map<String, Object> filters,
            String tenant
    ) {
        return repository.bulkDeleteByFilters(filters, tenant);
    }

    @Override
    public long countByFilters(Map<String, Object> filters, String tenant) {
        return repository.countByFilters(filters, tenant);
    }
}
