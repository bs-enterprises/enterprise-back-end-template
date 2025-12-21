package com.bs_enterprises.enterprise_backend_template.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JwtDecoder implementation that supports multiple issuers (realms).
 *
 * Strategy:
 *  - Peek into JWT payload (base64url) to extract "iss"
 *  - Create or reuse a NimbusJwtDecoder for that issuer using OIDC discovery (JwtDecoders.fromIssuerLocation)
 *  - Validate token using the issuer-specific decoder
 *
 * Security note: you should restrict allowed issuers (allowedIssuerPredicate) to avoid accepting tokens from arbitrary issuers.
 */
public class MultiIssuerJwtDecoder implements JwtDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cache of NimbusJwtDecoders per issuer
    private final ConcurrentMap<String, NimbusJwtDecoder> decoders = new ConcurrentHashMap<>();

    // TTL eviction: simple scheduled cleanup (optional)
    private final ConcurrentMap<String, Long> lastAccess = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    // Optional predicate to allow only certain issuers (recommended)
    private final java.util.function.Predicate<String> allowedIssuerPredicate;

    // Cache entry lifetime in millis
    private final long cacheTtlMillis;

    public MultiIssuerJwtDecoder(java.util.function.Predicate<String> allowedIssuerPredicate,
                                 Duration cacheTtl) {
        Assert.notNull(allowedIssuerPredicate, "allowedIssuerPredicate cannot be null");
        this.allowedIssuerPredicate = allowedIssuerPredicate;
        this.cacheTtlMillis = cacheTtl == null ? Duration.ofMinutes(30).toMillis() : cacheTtl.toMillis();

        // schedule periodic cleanup
        cleaner.scheduleAtFixedRate(this::evictOldEntries, cacheTtlMillis, cacheTtlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        String issuer = extractIssuer(token);
        if (issuer == null || issuer.isBlank()) {
            throw new JwtException("Unable to determine token issuer");
        }

        // Optional security gate: only allow tokens from issuers we trust
        if (!allowedIssuerPredicate.test(issuer)) {
            throw new JwtException("Token issuer is not allowed: " + issuer);
        }

        NimbusJwtDecoder decoder = decoders.computeIfAbsent(issuer, this::createDecoderForIssuer);
        lastAccess.put(issuer, System.currentTimeMillis());

        return decoder.decode(token);
    }

    private NimbusJwtDecoder createDecoderForIssuer(String issuer) {
        try {
            // JwtDecoders.fromIssuerLocation uses .well-known to find jwks_uri
            NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuer);

            // set default validators for this issuer (exp, nbf are validated by default)
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
            jwtDecoder.setJwtValidator(withIssuer);

            return jwtDecoder;
        } catch (Exception ex) {
            throw new JwtException("Failed to create JwtDecoder for issuer: " + issuer, ex);
        }
    }

    /**
     * Peek into the JWT payload (without verifying signature) to extract the "iss" claim.
     * Returns null if not found or token not a JWT.
     */
    private String extractIssuer(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = parts[1];
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            String payloadStr = new String(decoded, StandardCharsets.UTF_8);

            // quick & safe regex for "iss" string value
            Pattern p = Pattern.compile("\"iss\"\\s*:\\s*\"([^\"]+)\"");
            Matcher m = p.matcher(payloadStr);
            if (m.find()) {
                return m.group(1);
            }
            // fallback: attempt naive JSON-like search (in case whitespace/ordering different)
            int idx = payloadStr.indexOf("\"iss\"");
            if (idx >= 0) {
                int colon = payloadStr.indexOf(':', idx);
                if (colon > 0) {
                    int firstQuote = payloadStr.indexOf('"', colon);
                    if (firstQuote >= 0) {
                        int secondQuote = payloadStr.indexOf('"', firstQuote + 1);
                        if (secondQuote > firstQuote) {
                            return payloadStr.substring(firstQuote + 1, secondQuote);
                        }
                    }
                }
            }
            return null;
        } catch (IllegalArgumentException ex) {
            // invalid base64 / malformed token
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            // unexpected - return null to be safe
            return null;
        }
    }


    private void evictOldEntries() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> e : lastAccess.entrySet()) {
            if (now - e.getValue() > cacheTtlMillis) {
                String issuer = e.getKey();
                decoders.remove(issuer);
                lastAccess.remove(issuer);
            }
        }
    }

    // Call this to shutdown the cleaner on application stop (optional)
    public void shutdown() {
        cleaner.shutdownNow();
    }
}
