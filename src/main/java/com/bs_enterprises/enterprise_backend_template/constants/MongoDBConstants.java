package com.bs_enterprises.enterprise_backend_template.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MongoDBConstants {

    //Utility Ids
    public static final String ID_COMPANY_CONFIGURATION = "company-configuration";
    public static final String FIELD_ID = "_id";
    public static final String ID_HALF_EMAIL_VERIFICATION = "-email-verification";
    public static final String ID_HALF_RESET_PASSWORD = "-reset-password";

    // indices
    public static final String INDEX_EMAILS = "index_emails";
    public static final String INDEX_UIDS = "index_uids";
    public static final String INDEX_MOBILES = "index_mobiles";
    public static final String INDEX_PAN = "index_pan";
    public static final String INDEX_AADHAR = "index_aadhar";
    public static final String COLLECTION_ACCESS_TOKENS = "access_tokens";
    public static final String COLLECTION_USER_SECRETS = "user_secrets";

    public static final String COLLECTION_USERS = "users";

    public static final String COLLECTION_COMPANIES = "companies";
    public static final String COLLECTION_ABSENCE_APPLICATIONS = "absence_applications";


    @Getter
    private static final List<String> tokenIds = List.of(ID_HALF_EMAIL_VERIFICATION, ID_HALF_RESET_PASSWORD);


    // Roles Management Collections
    public static final String COLLECTION_ROLES_CONFIG = "roles_config";
    public static final String COLLECTION_RESOURCES = "resources";
    public static final String COLLECTION_ROLES_LIST = "roles_list";
    public static final String COLLECTION_USER_ROLES = "user_roles";
}