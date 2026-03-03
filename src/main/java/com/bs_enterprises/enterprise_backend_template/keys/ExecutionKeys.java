package com.bs_enterprises.enterprise_backend_template.keys;

import lombok.NoArgsConstructor;

/**
 * Execution-time message keys (used for runtime checks like uniqueness, not pre-validation).
 * Keys are wrapped in braces so they can be used directly with message resolvers if needed.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ExecutionKeys {

    public static final String REALM_REQUIRED = "execution.realm.required";

    public static final String USER_ID_ALREADY_EXISTS = "execution.user-id.already-exists";
    public static final String EMAIL_ALREADY_EXISTS = "execution.email.already-exists";
    public static final String PHONE_ALREADY_EXISTS = "execution.phone.already-exists";
    public static final String COMPANY_NOT_FOUND = "execution.company.not-found";
    public static final String USER_NOT_FOUND = "execution.user-management.user-not-found";

    public static final String FAILED_TO_UPDATE_USER = "execution.user-management.failed-to-update-user";
    public static final String FAILED_TO_DELETE_USER = "execution.user-management.failed-to-delete-user";
}
