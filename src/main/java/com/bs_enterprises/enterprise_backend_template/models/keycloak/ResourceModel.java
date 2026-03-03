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
 * Resource - Model for storing resource definitions in the new role management system
 * Resources are the top-level entities that have roles assigned to them
 *
 * Collection: resources
 * Fields:
 * - id: Unique identifier
 * - name: Resource name (e.g., "User Management", "Leave Management")
 * - description: Resource description
 * - createdAt: Timestamp of creation
 * - updatedAt: Timestamp of last update
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = MongoDBConstants.COLLECTION_RESOURCES)
public class ResourceModel {

    @Id
    @Field(MongoDBConstants.FIELD_ID)
    private String id;

    /** Resource name (e.g., "User Management", "Leave Management") */
    private String name;

    /** Optional description of the resource */
    private String description;

    /** Timestamp of resource creation */
    private Instant createdAt;

    /** Timestamp of last resource update */
    private Instant updatedAt;

    public static final List<String> allowedKeysForUpdate = List.of(
            "name",
            "description",
            "updatedAt"
    );

    public static final List<String> fieldsRequiringInstantConversion = List.of(
            "createdAt",
            "updatedAt"
    );
}

