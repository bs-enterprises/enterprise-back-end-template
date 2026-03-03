package com.bs_enterprises.enterprise_backend_template.controllers;

import com.bs_enterprises.enterprise_backend_template.models.responses.ApiResponse;
import com.bs_enterprises.enterprise_backend_template.repositories.BaseCrudService;
import com.bs_enterprises.enterprise_backend_template.services.common.MessageUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractCrudController<E, C>
        implements BaseCrudController<E, C> {

    protected final BaseCrudService<E> service;
    protected final MessageUtils messageUtils;
    protected final ObjectMapper objectMapper;

    // ---- hooks for child controllers ----
    protected abstract String createdKey();
    protected abstract String updatedKey();
    protected abstract String fetchedKey();
    protected abstract String listedKey();
    protected abstract String deletedKey();
    protected abstract String bulkUpdatedKey();
    protected abstract String bulkDeletedKey();

    protected abstract E createFromCarrier(C carrier, String tenant);

    // ---------------- CREATE ----------------
    @Override
    public ApiResponse<E> create(C carrier, String tenant) {
        E created = createFromCarrier(carrier, tenant);

        return ApiResponse.success(
                createdKey(),
                messageUtils.getMessageFromCode(createdKey()),
                created
        );
    }

    // ---------------- UPDATE ----------------
    @Override
    public ApiResponse<E> update(String id, Map<String, Object> updates, String tenant) {
        E updated = service.update(id, updates, tenant);

        return ApiResponse.success(
                updatedKey(),
                messageUtils.getMessageFromCode(updatedKey()),
                updated
        );
    }

    // ---------------- GET BY ID ----------------
    @Override
    public ApiResponse<E> getById(String id, String tenant) {
        E data = service.getById(id, tenant);

        return ApiResponse.success(
                fetchedKey(),
                messageUtils.getMessageFromCode(fetchedKey()),
                data
        );
    }

    // ---------------- SEARCH ----------------
    @Override
    public ApiResponse<Page<E>> search(
            Map<String, Object> searchParams,
            int page,
            int size,
            String tenant
    ) {
        Page<E> result = service.search(searchParams, page, size, tenant);

        return ApiResponse.success(
                listedKey(),
                messageUtils.getMessageFromCode(listedKey()),
                result
        );
    }

    // ---------------- DELETE ----------------
    @Override
    public ApiResponse<Void> delete(String id, String tenant) {
        service.delete(id, tenant);

        return ApiResponse.success(
                deletedKey(),
                messageUtils.getMessageFromCode(deletedKey())
        );
    }

    // ---------------- BULK UPDATE ----------------
    @Override
    public ApiResponse<Long> bulkUpdate(Map<String, Object> request, String tenant) {
        Map<String, Object> filters = objectMapper.convertValue(
                request.get("filters"),
                new TypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> updates = objectMapper.convertValue(
                request.get("updates"),
                new TypeReference<Map<String, Object>>() {}
        );

        long count = service.bulkUpdateByFilters(filters, updates, tenant);

        return ApiResponse.success(
                bulkUpdatedKey(),
                messageUtils.getMessageFromCode(bulkUpdatedKey()),
                count
        );
    }

    // ---------------- BULK DELETE BY IDS ----------------
    @Override
    public ApiResponse<Long> bulkDeleteByIds(List<String> ids, String tenant) {
        long count = service.bulkDeleteByIds(ids, tenant);

        return ApiResponse.success(
                bulkDeletedKey(),
                messageUtils.getMessageFromCode(bulkDeletedKey()),
                count
        );
    }

    // ---------------- BULK DELETE BY FILTERS ----------------
    @Override
    public ApiResponse<Long> bulkDeleteByFilters(
            Map<String, Object> filters,
            String tenant
    ) {
        long count = service.bulkDeleteByFilters(filters, tenant);

        return ApiResponse.success(
                bulkDeletedKey(),
                messageUtils.getMessageFromCode(bulkDeletedKey()),
                count
        );
    }
}
