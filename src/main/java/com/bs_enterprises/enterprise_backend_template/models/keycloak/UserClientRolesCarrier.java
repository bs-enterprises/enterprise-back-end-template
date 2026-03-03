package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * Carrier for assigning or removing client roles to/from a user
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClientRolesCarrier {

    @NotBlank(message = "userId is required")
    private String userId;

    private String username;

    @NotBlank(message = "clientId is required")
    private String clientId;

    @NotNull(message = "roleNames is required")
    @NotEmpty(message = "roleNames cannot be empty")
    private List<String> roleNames;
}

