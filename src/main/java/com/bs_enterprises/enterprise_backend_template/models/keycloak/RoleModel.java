package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

/**
 * RolesList - Model for storing roles associated with resources in the new role management system
 * This collection contains the detailed role information for each resource
 *
 * Collection: roles_list
 * Fields:
 * - id: Unique identifier
 * - resourceId: Reference to the Resource this role belongs to
 * - role: The role identifier/key
 * - roleName: Human-readable role name
 * - description: Role description
 * - createdAt: Timestamp of creation
 * - updatedAt: Timestamp of last update
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = MongoDBConstants.COLLECTION_ROLES_LIST)
public class RoleModel {

    @Id
    @Field(MongoDBConstants.FIELD_ID)
    private String id; // this acts as role

    /** Reference to the Resource this role belongs to */
    private String resourceId;

    /** Human-readable role name (e.g., "Administrator", "User", "Viewer") */
    private String roleName;

    /** Optional description of the role */
    private String description;

    /** Timestamp of role creation */
    private Instant createdAt;

    /** Timestamp of last role update */
    private Instant updatedAt;

    public static final List<String> allowedKeysForUpdate = List.of(
            "resourceId",
            "roleName",
            "description",
            "updatedAt"
    );

    public static final List<String> fieldsRequiringInstantConversion = List.of(
            "createdAt",
            "updatedAt"
    );
}

