package com.bs_enterprises.enterprise_backend_template.models.users;

import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import com.bs_enterprises.enterprise_backend_template.keys.ValidationKeys;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal DTO required to create a Keycloak user.
 * Validation messages are externalized to message properties via ValidationKeys.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = MongoDBConstants.COLLECTION_USERS)
public class KeycloakUserModel {

    @Id
    @Field(MongoDBConstants.FIELD_ID)
    private String id;  // Keycloak ID or UUID

    @NotBlank(message = ValidationKeys.USERNAME_REQUIRED)
    @Size(min = 3, max = 100, message = ValidationKeys.USERNAME_SIZE)
    private String username;

    @Email(message = ValidationKeys.EMAIL_INVALID)
    private String email;

    /**
     * Phone in E.164 format
     * ex: +919876543210
     */
    @Pattern(
            regexp = "^\\+?[1-9]\\d{1,14}$",
            message = ValidationKeys.PHONE_INVALID
    )
    private String phone;

    /**
     * Keycloak requires firstName and lastName instead of a single fullName.
     */
    @NotBlank(message = ValidationKeys.FIRST_NAME_REQUIRED)
    private String firstName;

    @NotBlank(message = ValidationKeys.LAST_NAME_REQUIRED)
    private String lastName;

    /**
     * If true → user can immediately log in using OTP flow.
     */
    private boolean enabled = true;

    /**
     * Whether the user should verify email (usually false for admin-created).
     */
    private boolean emailVerified = false;

    public static List<String> allowedKeysForUpdate = List.of(
            "username",
            "firstName",
            "lastName",
            "email",
            "phone",
            "enabled",
            "emailVerified"
    );
}
