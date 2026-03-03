package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import lombok.*;

import java.util.List;

/**
 * Represents a user's assigned client roles
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClientRolesInfo {

    private String userId;
    private String clientId;
    private List<String> assignedRoles;
}

