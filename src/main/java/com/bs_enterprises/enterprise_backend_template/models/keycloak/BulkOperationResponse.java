package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import lombok.*;

import java.util.List;

/**
 * Response model for Keycloak bulk operations
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationResponse {

    private String action;
    private List<String> success;
    private List<String> notFound;
    private Integer successCount;
    private Integer notFoundCount;
}
