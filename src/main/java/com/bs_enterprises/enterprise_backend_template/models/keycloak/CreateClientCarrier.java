package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Carrier for creating a Keycloak client
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientCarrier {

    @NotBlank(message = "clientId is required")
    private String clientId;

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    private boolean confidential;
}

