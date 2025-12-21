package com.bs_enterprises.enterprise_backend_template.models.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class TokenResponseModel {

    @JsonProperty("access_token")
    private String accessToken;        // Maps "access_token" JSON field to accessToken Java field

    @JsonProperty("expires_in")
    private long expiresIn;            // Maps "expires_in" JSON field to expiresIn Java field

    @JsonProperty("refresh_expires_in")
    private long refreshExpiresIn;     // Maps "refresh_expires_in" JSON field to refreshExpiresIn Java field

    @JsonProperty("refresh_token")
    private String refreshToken;       // Maps "refresh_token" JSON field to refreshToken Java field

    @JsonProperty("token_type")
    private String tokenType;          // Maps "token_type" JSON field to tokenType Java field

    @JsonProperty("not-before-policy")
    private long notBeforePolicy;      // Maps "not-before-policy" JSON field to notBeforePolicy Java field

    @JsonProperty("session_state")
    private String sessionState;       // Maps "session_state" JSON field to sessionState Java field

    @JsonProperty("scope")
    private String scope;              // Maps "scope" JSON field to scope Java field


    @Getter
    public enum TokenResponseProperties {

        EXP("exp"),
        IAT("iat"),
        JTI("jti"),
        ISS("iss"),
        AUD("aud"),
        SUB("sub"),
        TYP("typ"),
        AZP("azp"),
        SESSION_STATE("session_state"),
        SCOPE("scope"),
        SID("sid"),
        EMAIL_VERIFIED("email_verified"),
        NAME("name"),
        PREFERRED_USERNAME("preferred_username"),
        GIVEN_NAME("given_name"),
        FAMILY_NAME("family_name"),
        EMAIL("email"),
        RESOURCE_ACCESS("resource_access");

        private final String jsonKey;

        // Constructor to initialize the jsonKey for each enum constant
        TokenResponseProperties(String jsonKey) {
            this.jsonKey = jsonKey;
        }
    }

}