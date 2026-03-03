package com.bs_enterprises.enterprise_backend_template.models.keycloak;

import lombok.*;

import java.util.List;

/**
 * Represents all clients and their roles in a Keycloak realm
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealmClientsAndRoles {

    private String realm;
    private Integer clientCount;
    private List<ClientRoleInfo> clients;
}

