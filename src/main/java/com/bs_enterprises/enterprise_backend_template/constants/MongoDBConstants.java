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
    public static final String COLLECTION_ACCESS_TOKENS = "access_tokens";
    public static final String COLLECTION_USER_SECRETS = "user_secrets";
    public static final String COLLECTION_USERS_SNAPSHOTS = "users_snapshots";

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_MEMBERS_SNAPSHOTS = "members_snapshots";
    public static final String COLLECTION_STAFF_SNAPSHOTS = "staff_snapshots";
    public static final String COLLECTION_USER_GENERAL_DETAILS = "user_general_details";

    public static final String COLLECTION_STUDIOS = "studios";

    public static final String COLLECTION_MEMBERSHIP_PLANS = "membership_plans";
    public static final String COLLECTION_MEMBERSHIP_FEATURES = "membership_features";

    @Getter
    private static final List<String> tokenIds = List.of(ID_HALF_EMAIL_VERIFICATION, ID_HALF_RESET_PASSWORD);

    public static final String COLLECTION_AUTO_GEN_POLICIES = "auto_gen_policies";
}