package com.bs_enterprises.enterprise_backend_template.constants;

import lombok.Getter;

public class KeycloakConstants {

    // Other Constants
    public static final String USER_DATA = "userData";
    public static final String USER_ID = "userId";
    public static final String GROUPS = "groups";
    public static final String ROLES = "roles";
    public static final String ROLES_DELETE = "deleteRoles";
    public static final String KEYCLOAK_USER_ID = "keycloakUserId";
    public static final String PASSWORD = "password";
    public static final String EMAIL_VERIFIED = "emailVerified";

    public static final String USER_NAME_CAMELCASE = "userName";
    public static final String USER_NAME_LOWERCASE = "username";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String ENABLED = "enabled";
    public static final String EMAIL_VERIFICATION_LINK = "emailVerificationLink";
    public static final String TOKEN_GRANT_TYPE = "grant_type";
    public static final String TOKEN_CLIENT_ID = "client_id";

    @Getter
    public enum KeycloakPermissionsTypes {
        TYPE_GROUPS(GROUPS),
        TYPE_ROLES(ROLES);

        private final String key;

        KeycloakPermissionsTypes(String key) {
            this.key = key;
        }

    }

}