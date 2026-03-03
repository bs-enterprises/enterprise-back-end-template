package com.bs_enterprises.enterprise_backend_template.controllers;

import com.bs_enterprises.enterprise_backend_template.constants.ApplicationConstants;
import com.bs_enterprises.enterprise_backend_template.models.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

public interface BaseCrudController<E, C> {

    // CREATE
    @PostMapping
    ApiResponse<E> create(
            @RequestBody @Valid C carrier,
            @RequestHeader(ApplicationConstants.HEADER_X_TENANT_ID) String tenant
    );

    // UPDATE
    @PatchMapping("/{id}")
    ApiResponse<E> update(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates,
            @RequestHeader(ApplicationConstants.HEADER_X_TENANT_ID) String tenant
    );

    // GET BY ID
    @GetMapping("/{id}")
    ApiResponse<E> getById(
            @PathVariable String id,
            @RequestHeader(ApplicationConstants.HEADER_X_TENANT_ID) String tenant
    );

    // SEARCH
    @PostMapping("/search")
    ApiResponse<Page<E>> search(
            @RequestBody Map<String, Object> searchParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(ApplicationConstants.HEADER_X_TENANT_ID) String tenant
    );

    // DELETE
    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(
            @PathVariable String id,
            @RequestHeader(ApplicationConstants.HEADER_X_TENANT_ID) String tenant
    );

    // BULK UPDATE
    @PatchMapping("/bulk-update")
    ApiResponse<Long> bulkUpdate(
            @RequestBody Map<String, Object> request,
            @RequestHeader(ApplicationConstants.HEADER_X_TENANT_ID) String tenant
    );

    // BULK DELETE BY IDS
    @DeleteMapping("/bulk-delete-by-ids")
    ApiResponse<Long> bulkDeleteByIds(
            @RequestBody List<String> ids,
            @RequestHeader(ApplicationConstants.HEADER_X_TENANT_ID) String tenant
    );

    // BULK DELETE BY FILTERS
    @PostMapping("/bulk-delete-by-filters")
    ApiResponse<Long> bulkDeleteByFilters(
            @RequestBody Map<String, Object> filters,
            @RequestHeader(ApplicationConstants.HEADER_X_TENANT_ID) String tenant
    );
}
