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
 * RolesConfig - Persisted model for storing resource-roles configuration in the new role management system
 *
 * This model links resources with their associated roles from the roles_list collection.
 * It serves as a junction/config model that maintains the mapping between resources and their roles.
 *
 * Structure:
 * - id: Unique identifier
 * - realm: The realm this configuration belongs to
 * - resourceId: Reference to the Resource this config is for
 * - roleIds: List of role IDs (references to RolesList.id) assigned to this resource
 * - createdAt: Timestamp of creation
 * - updatedAt: Timestamp of last update
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = MongoDBConstants.COLLECTION_ROLES_CONFIG)
public class RolesConfig {

    @Id
    @Field(MongoDBConstants.FIELD_ID)
    private String id; // the resourceId

    /** The realm this resource-roles config belongs to */
    private String realm;

    /** Timestamp of configuration creation */
    private Instant createdAt;

    /** Timestamp of last configuration update */
    private Instant updatedAt;

    public static final List<String> allowedKeysForUpdate = List.of(
            "updatedAt"
    );

    public static final List<String> fieldsRequiringInstantConversion = List.of(
            "createdAt",
            "updatedAt"
    );
}

