package com.bs_enterprises.enterprise_backend_template.models.tokens;

/**
 * UsernameAndRealm - Record to hold username and realm information from JWT token
 * Provides a clean way to return both values together from token context
 *
 * @param username The preferred username from the token (preferred_username claim)
 * @param realm The realm name extracted from the issuer URL
 */
public record UsernameAndRealm(String username, String realm) {
}

