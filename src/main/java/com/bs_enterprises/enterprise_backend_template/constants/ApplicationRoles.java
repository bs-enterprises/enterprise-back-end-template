package com.bs_enterprises.enterprise_backend_template.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationRoles {

    private static final String HAS_ANY_AUTHORITY_START = "hasAnyAuthority('";
    private static final String HAS_ANY_AUTHORITY_END = "')";
    private static final String ISSUED_MASTER_CREATE_REALM = "ISSUED_MASTER_CREATE-REALM";

    // Publicly accessible roles
    public static final String ROLE_ISSUED_MASTER_CREATE_REALM =
            HAS_ANY_AUTHORITY_START + ISSUED_MASTER_CREATE_REALM + HAS_ANY_AUTHORITY_END;

}
