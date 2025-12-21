package com.bs_enterprises.enterprise_backend_template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "company")
public class CompanyProperties {
    private String name;
    private String websiteUrl;
    private String supportEmail;
    private List<String> origin;
    private String verifyEmailRoute;
    private String resetPasswordRoute;
}
