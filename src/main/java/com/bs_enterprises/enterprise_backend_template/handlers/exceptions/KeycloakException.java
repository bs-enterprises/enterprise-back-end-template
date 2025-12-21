package com.bs_enterprises.enterprise_backend_template.handlers.exceptions;

public class KeycloakException extends RuntimeException {
    private final String error;
    private final String description;

    public KeycloakException(String error, String description) {
        super(description);
        this.error = error;
        this.description = description;
    }

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }
}
