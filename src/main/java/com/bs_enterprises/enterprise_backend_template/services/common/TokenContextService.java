package com.bs_enterprises.enterprise_backend_template.services.common;

import com.bs_enterprises.enterprise_backend_template.models.tokens.UsernameAndRealm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TokenContextService - Service to extract user information from JWT token context
 * Provides centralized access to authenticated user details without requiring explicit parameters
 */
@Slf4j
@Service
public class TokenContextService {

    /**
     * Get the currently authenticated user's JWT token
     *
     * @return Jwt token or null if not authenticated
     */
    public Jwt getJwtToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                return (Jwt) authentication.getPrincipal();
            }
            log.warn("No JWT token found in security context");
            return null;
        } catch (Exception e) {
            log.error("Error retrieving JWT token from context: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get the Bearer token string (for Authorization header)
     * Returns "Bearer {token}" format
     *
     * @return Bearer token string or null if not authenticated
     */
    public String getBearerToken() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get bearer token: JWT token is null");
                return null;
            }
            return "Bearer " + jwt.getTokenValue();
        } catch (Exception e) {
            log.error("Error extracting bearer token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get employee ID from token attributes
     * Keycloak stores custom attributes that can include employee ID
     *
     * @return Employee ID or null if not found
     */
    public String getEmployeeId() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get employee ID: JWT token is null");
                return null;
            }

            // Try to get employee ID from attributes
            Map<String, Object> claims = jwt.getClaims();

            // Check common attribute locations for employee ID
            if (claims.containsKey("employeeId")) {
                return (String) claims.get("employeeId");
            }

            if (claims.containsKey("employee_id")) {
                return (String) claims.get("employee_id");
            }

            // Check in custom attributes map
            if (claims.containsKey("attributes")) {
                Map<String, Object> attributes = (Map<String, Object>) claims.get("attributes");
                if (attributes != null && attributes.containsKey("employeeId")) {
                    Object empId = attributes.get("employeeId");
                    if (empId instanceof List) {
                        List<String> empIdList = (List<String>) empId;
                        return empIdList.isEmpty() ? null : empIdList.get(0);
                    }
                    return (String) empId;
                }
            }

            // Fallback to subject (user ID) if employee ID not found
            String sub = jwt.getSubject();
            log.debug("Employee ID not found in token, using subject as fallback: {}", sub);
            return sub;

        } catch (Exception e) {
            log.error("Error extracting employee ID from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get user ID (subject) from token
     *
     * @return User ID (sub claim) or null if not found
     */
    public String getUserId() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get user ID: JWT token is null");
                return null;
            }
            return jwt.getSubject();
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get user's email from token
     *
     * @return Email or null if not found
     */
    public String getEmail() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get email: JWT token is null");
                return null;
            }
            return jwt.getClaimAsString("email");
        } catch (Exception e) {
            log.error("Error extracting email from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get user's preferred username from token
     *
     * @return Preferred username or null if not found
     */
    public String getPreferredUsername() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get preferred username: JWT token is null");
                return null;
            }
            return jwt.getClaimAsString("preferred_username").toUpperCase();
        } catch (Exception e) {
            log.error("Error extracting preferred username from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get user's first name from token
     *
     * @return First name or null if not found
     */
    public String getFirstName() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get first name: JWT token is null");
                return null;
            }
            return jwt.getClaimAsString("given_name");
        } catch (Exception e) {
            log.error("Error extracting first name from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get user's last name from token
     *
     * @return Last name or null if not found
     */
    public String getLastName() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get last name: JWT token is null");
                return null;
            }
            return jwt.getClaimAsString("family_name");
        } catch (Exception e) {
            log.error("Error extracting last name from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get user's full name from token
     *
     * @return Full name or null if not found
     */
    public String getFullName() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get full name: JWT token is null");
                return null;
            }
            return jwt.getClaimAsString("name");
        } catch (Exception e) {
            log.error("Error extracting full name from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get all roles from the authenticated user
     * Returns roles with ROLE_ prefix removed for easier checking
     *
     * @return List of role names (without ROLE_ prefix)
     */
    public List<String> getRoles() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                log.warn("No authentication found in security context");
                return List.of();
            }

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            return authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error extracting roles from context: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get roles from specific resource access (e.g., user-management)
     *
     * @param clientId The client ID to get roles for (e.g., "user-management")
     * @return List of roles for the specified client
     */
    @SuppressWarnings("unchecked")
    public List<String> getResourceRoles(String clientId) {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get resource roles: JWT token is null");
                return List.of();
            }

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess == null || !resourceAccess.containsKey(clientId)) {
                log.debug("No resource access found for client: {}", clientId);
                return List.of();
            }

            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
            if (clientAccess == null || !clientAccess.containsKey("roles")) {
                log.debug("No roles found for client: {}", clientId);
                return List.of();
            }

            return (List<String>) clientAccess.get("roles");
        } catch (Exception e) {
            log.error("Error extracting resource roles from token: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Check if user has a specific role
     *
     * @param role Role name to check (without ROLE_ prefix)
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        try {
            List<String> roles = getRoles();
            return roles.contains(role) || roles.contains(role.toUpperCase());
        } catch (Exception e) {
            log.error("Error checking role '{}': {}", role, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if user has any of the specified roles
     *
     * @param roles Roles to check (without ROLE_ prefix)
     * @return true if user has at least one of the roles, false otherwise
     */
    public boolean hasAnyRole(String... roles) {
        try {
            List<String> userRoles = getRoles();
            for (String role : roles) {
                if (userRoles.contains(role) || userRoles.contains(role.toUpperCase())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking any roles: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if user has all of the specified roles
     *
     * @param roles Roles to check (without ROLE_ prefix)
     * @return true if user has all the roles, false otherwise
     */
    public boolean hasAllRoles(String... roles) {
        try {
            List<String> userRoles = getRoles();
            for (String role : roles) {
                if (!userRoles.contains(role) && !userRoles.contains(role.toUpperCase())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error checking all roles: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get a specific claim from the token
     *
     * @param claimName Name of the claim
     * @return Claim value or null if not found
     */
    public Object getClaim(String claimName) {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get claim '{}': JWT token is null", claimName);
                return null;
            }
            return jwt.getClaim(claimName);
        } catch (Exception e) {
            log.error("Error extracting claim '{}' from token: {}", claimName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get a specific claim as String from the token
     *
     * @param claimName Name of the claim
     * @return Claim value as String or null if not found
     */
    public String getClaimAsString(String claimName) {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get claim '{}': JWT token is null", claimName);
                return null;
            }
            return jwt.getClaimAsString(claimName);
        } catch (Exception e) {
            log.error("Error extracting claim '{}' as string from token: {}", claimName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get all claims from the token
     *
     * @return Map of all claims or empty map if token is null
     */
    public Map<String, Object> getAllClaims() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get all claims: JWT token is null");
                return Map.of();
            }
            return jwt.getClaims();
        } catch (Exception e) {
            log.error("Error extracting all claims from token: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * Check if the current user is authenticated
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && authentication.isAuthenticated();
        } catch (Exception e) {
            log.error("Error checking authentication status: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get issuer from token
     *
     * @return Issuer URL or null if not found
     */
    public String getIssuer() {
        try {
            Jwt jwt = getJwtToken();
            if (jwt == null) {
                log.warn("Cannot get issuer: JWT token is null");
                return null;
            }
            return jwt.getClaimAsString("iss");
        } catch (Exception e) {
            log.error("Error extracting issuer from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get realm name from issuer URL
     * Extracts realm name from issuer like "http://localhost:8080/realms/test-realm" -> "test-realm"
     *
     * @return Realm name or null if not found
     */
    public String getRealmName() {
        try {
            String issuer = getIssuer();
            if (issuer == null) {
                return null;
            }
            // Extract realm name from issuer URL (last segment)
            int lastSlashIndex = issuer.lastIndexOf("/");
            if (lastSlashIndex != -1 && lastSlashIndex < issuer.length() - 1) {
                return issuer.substring(lastSlashIndex + 1);
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting realm name from issuer: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get username and realm together from the token
     * Combines preferred_username claim with extracted realm name from issuer URL
     *
     * @return UsernameAndRealm record containing both username and realm
     */
    public UsernameAndRealm getUsernameAndRealm() {
        try {
            String username = getPreferredUsername();
            String realm = getRealmName();

            if (username == null) {
                log.warn("Cannot get username and realm: username is null");
                return new UsernameAndRealm(null, realm);
            }

            return new UsernameAndRealm(username, realm);
        } catch (Exception e) {
            log.error("Error extracting username and realm from token: {}", e.getMessage(), e);
            return new UsernameAndRealm(null, null);
        }
    }

    /**
     * Log current token context information (for debugging)
     */
    public void logTokenContext() {
        log.info("=== Token Context Information ===");
        log.info("Authenticated: {}", isAuthenticated());
        log.info("User ID: {}", getUserId());
        log.info("Employee ID: {}", getEmployeeId());
        log.info("Email: {}", getEmail());
        log.info("Full Name: {}", getFullName());
        log.info("Roles: {}", getRoles());
        log.info("Realm: {}", getRealmName());
        log.info("Issuer: {}", getIssuer());
        log.info("================================");
    }
}
