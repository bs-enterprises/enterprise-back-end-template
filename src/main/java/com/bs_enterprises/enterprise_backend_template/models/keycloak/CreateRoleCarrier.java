package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Carrier for creating a role inside a Keycloak client
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleCarrier {

    @NotBlank(message = "roleName is required")
    private String roleName;

    private String description;
}

