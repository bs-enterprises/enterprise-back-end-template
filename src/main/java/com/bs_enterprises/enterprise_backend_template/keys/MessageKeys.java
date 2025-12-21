package com.bs_enterprises.enterprise_backend_template.keys;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageKeys {

    // Token error message keys
    public static final String ERROR_TOKEN_MISSING = "error.token.missing";
    public static final String ERROR_TYPE_MISSING = "error.type.missing";
    public static final String ERROR_TOKEN_INVALID_TYPE = "error.token.invalid-type";
    public static final String ERROR_TOKEN_EXPIRED = "error.token.expired";
    public static final String ERROR_ACCESS_DENIED = "error.access.denied";

    // Access error message keys
    public static final String ERROR_ACCESS_UNAUTHORIZED = "error.access.unauthorized";
    public static final String ERROR_ACCESS_FORBIDDEN = "error.access.forbidden";
    public static final String ERROR_TOKEN_VERIFICATION_FAILED = "error.token-verification-failed";
    public static final String ERROR_INVALID_CREDENTIALS_OR_UNAUTHORIZED = "error.invalid-credentials-or-unauthorized";
    // Unknown error message key
    public static final String ERROR_UNKNOWN_TRY_LATER = "error.unknown.try-later";
    public static final String ERROR_UNKNOWN_CONTACT_ADMINISTRATOR = "error.unknown.contact-administrator";
    public static final String ERROR_KNOWN_HEADER_MISSING = "error.known.header-missing";
    public static final String ERROR_AUTH_EMAIL_ALREADY_VERIFIED = "error.auth.email.already-verified";
    // common
    public static final String COMMON_INVALID_PROPERTY_SENT = "common.error.invalid-property-sent";

    // Response success message keys
    public static final String RESPONSE_SUCCESS_USER_CREATED = "response.success.user-created";
    public static final String RESPONSE_SUCCESS_LOGIN_SUCCESS = "response.success.login-success";

    // Validation error message key
    public static final String ERROR_VALIDATION = "error.validation";
    public static final String ERROR_PAYLOAD_VALIDATION = "error.payload.validation";
    // AuthService specific error keys
    public static final String ERROR_USER_ALREADY_EXISTS = "error.auth.user-already-exists";

    public static final String ERROR_INVALID_REQUEST = "response.error.invalid-request";
}