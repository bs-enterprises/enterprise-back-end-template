package com.bs_enterprises.enterprise_backend_template.models.users;

public record LoadedArtifacts(KeycloakUserModel kcUser, UserSecrets secrets) { }
