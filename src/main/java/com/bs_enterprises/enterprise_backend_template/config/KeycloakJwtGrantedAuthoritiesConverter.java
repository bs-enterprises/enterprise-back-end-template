package com.bs_enterprises.enterprise_backend_template.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("unchecked")
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NotNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Extract roles from 'resource_access' and 'realm_access' claims
        authorities.addAll(extractRolesFromAllResourceAccess(jwt));
        authorities.addAll(extractRolesFromRealmAccess(jwt));

        return authorities;
    }

    private Collection<GrantedAuthority> extractRolesFromAllResourceAccess(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");
        if (resourceAccess != null) {
            for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                String realmOrClient = entry.getKey();
                Map<String, Object> clientRoles = (Map<String, Object>) entry.getValue();
                Collection<String> roles = (Collection<String>) clientRoles.get("roles");
                if (roles != null) {
                    authorities.addAll(
                            roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("REALM_" + realmOrClient.toUpperCase() + "_" + role.toUpperCase()))
                                    .toList()
                    );
                }
            }
        }

        return authorities;
    }

    private Collection<GrantedAuthority> extractRolesFromRealmAccess(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        String issuer = (String) jwt.getClaims().get("iss"); // e.g. http://localhost:8080/realms/master
        String realm = issuer.substring(issuer.lastIndexOf("/") + 1); // master
        authorities.add(new SimpleGrantedAuthority("ISSUED_REALM_" + realm.toUpperCase()));
        if (realmAccess != null) {
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            if (roles != null) {
                authorities.addAll(
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ISSUED_" + realm.toUpperCase() + "_" + role.toUpperCase()))
                                .toList()
                );
            }
        }

        return authorities;
    }

    private void addRoles(Collection<GrantedAuthority> authorities, Map<String, Object> realmAccess) {
        if (realmAccess != null) {
            Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                authorities.addAll(realmRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .toList());
            }
        }
    }

}
