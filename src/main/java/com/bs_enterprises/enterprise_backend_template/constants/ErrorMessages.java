package com.bs_enterprises.enterprise_backend_template.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessages {

    public static final String PLEASE_CONTACT_SUPPORT = "Please contact support";
    public static final String USER_CREATION_FAILED = "Failed to create user";

    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";

    public static final String PLEASE_PROVIDE_USERNAME_AND_PASSWORD = "Please provide username and password";
    public static final String NETWORK_ERROR_OCCURRED_DURING_TOKEN_GENERATION = "Network error occurred during token generation";
    public static final String INVALID_TOKEN = "Invalid token";

    public static final String INVALID_PROPERTY_SENT = "Invalid Property Sent";
    public static final String DOCUMENT_WAS_NOT_SAVED_SUCCESSFULLY = "Document was not saved successfully.";
    public static final String DOCUMENT_NOT_FOUND = "Document not found";
    public static final String NO_PROPERTIES_FOUND_FOR_THE_GIVEN_TYPE = "No properties found for the given type.";
    public static final String USER_APPROVAL_RECORD_NOT_FOUND_FOR_UID = "User approval record not found for UID: ";
    public static final String FAILED_TO_CREATE_IN_THE_DATABASE = "Failed to create in the database";
    public static final String FAILED_TO_UPDATE_IN_THE_DATABASE = "Failed to update in the database";
    public static final String FAILED_TO_DELETE_IN_THE_DATABASE = "Failed to delete in the database";
    public static final String FAILED_TO_RETRIEVE_IN_THE_DATABASE = "Failed to retrieve in the database";
    public static final String PHONE_NUMBER_ALREADY_EXISTS = "Phone number already exists";
    public static final String ALTERNATIVE_PHONE_NUMBER_ALREADY_EXISTS = "Alternative Phone number already exists";
    public static final String AUTHENTICATION_SERVER_CURRENTLY_NOT_AVAILABLE = "Authentication Server currently not available";
}