package com.bs_enterprises.enterprise_backend_template.keys;

import lombok.NoArgsConstructor;

/**
 * Database-level execution message keys:
 * - CRUD failures
 * - Record-not-found cases
 * - Insert/update/delete failures
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class DatabaseKeys {

    public static final String CREATION_FAILED = "database.creation.failed";
    public static final String UPDATE_FAILED = "database.update.failed";
    public static final String DELETE_FAILED = "database.delete.failed";

    public static final String RECORD_NOT_FOUND = "database.record.not-found";
    public static final String RECORD_ALREADY_EXISTS = "database.record.already-exists";

    public static final String INVALID_UPDATE_PAYLOAD = "database.invalid-update-payload";
    public static final String INVALID_QUERY_PARAMETERS = "database.invalid-query-parameters";

    // Add more keys as you introduce new database behaviors
}
