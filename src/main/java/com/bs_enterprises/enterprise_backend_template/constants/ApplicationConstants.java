package com.bs_enterprises.enterprise_backend_template.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationConstants {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String PROPERTY_PREFIX_COMPANY = "company";


    //JWTRoleConverter
    public static final String CLIENT_ROLES = "clientRoles";
    public static final String USER_INFO_URL = "/protocol/openid-connect/userinfo";
    public static final String TOKEN_VERIFICATION_FAILED = "Token verification failed";
    public static final String CLIENT_ROLES_MISSING_IN_USER_INFORMATION = "ClientRoles are missing in the userInformation";


    /*TokenUtilsAndWebclientWrapperConstants*/
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
    public static final String EMAIL = "email";
    public static final String UNABLE_GET_TOKEN = "Unable to get token";
    public static final String EMPTY_STRING = "";
    public static final String BEARER = "Bearer ";
    public static final String REGEX_SPLIT = "\\.";
    public static final String ISS = "iss";
    public static final String URL_SEPERATOR = "/";
    public static final int SEVEN = 7;
    public static final int ONE = 1;

    /*PropertiesServiceImplConstants*/
    public static final String SPACE = " ";

    /*UserDetailsConstants*/
    public static final String AUTHORIZATION = "Authorization";


    public static final String COMMA = ",";

    /*FormControllerConstants*/
    public static final String ID = "id";
    //WebClientWrapper
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String PATCH = "PATCH";

    public static final String HEADER_X_CLIENT_TYPE = "X-Client-Type";

    public static final String HEADER_X_TENANT_ID = "X-Tenant-Id";
}