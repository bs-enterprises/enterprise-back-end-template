package com.bs_enterprises.enterprise_backend_template.keys;

import lombok.NoArgsConstructor;

/**
 * Centralized validation message keys used by Jakarta Validation annotations.
 * Each constant contains the braces so it can be used directly in annotation 'message' attributes.
 * <p>
 * e.g. @NotBlank(message = ValidationKeys.USERNAME_REQUIRED)
 * <p>
 * The corresponding keys must exist in your messages.properties file (without braces).
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ValidationKeys {

    // existing user keys (kept and wrapped in braces)
    public static final String USERNAME_REQUIRED = "validation.user.username.required";
    public static final String USERNAME_SIZE = "validation.user.username.size";
    public static final String EMAIL_INVALID = "validation.user.email.invalid";
    public static final String PHONE_INVALID = "validation.user.phone.invalid";
    public static final String FIRST_NAME_REQUIRED = "validation.user.first-name.required";
    public static final String LAST_NAME_REQUIRED = "validation.user.last-name.required";

}
