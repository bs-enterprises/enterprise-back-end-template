package com.bs_enterprises.enterprise_backend_template.config;

import com.bs_enterprises.enterprise_backend_template.constants.PropertiesConfigPrefixes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Service
@ConfigurationProperties(prefix = PropertiesConfigPrefixes.KEYCLOAK)
public class KeycloakPropertiesConfiguration {

    private String issuerUri;
}