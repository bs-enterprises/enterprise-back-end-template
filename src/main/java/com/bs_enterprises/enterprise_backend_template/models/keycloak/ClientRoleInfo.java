package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import lombok.*;

import java.util.List;

/**
 * Represents a single Keycloak client with its associated roles
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRoleInfo {

    private String id;
    private String clientId;
    private String name;
    private String description;
    private Boolean enabled;
    private List<String> roles;
}

