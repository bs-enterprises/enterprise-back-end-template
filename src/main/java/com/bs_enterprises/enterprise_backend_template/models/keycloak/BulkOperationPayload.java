package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * Payload for Keycloak bulk operations (deactivate, enable, delete)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationPayload {

    @NotNull(message = "identifiers cannot be null")
    @NotEmpty(message = "identifiers cannot be empty")
    private List<String> identifiers;
}
