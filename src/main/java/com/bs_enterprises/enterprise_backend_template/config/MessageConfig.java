package com.bs_enterprises.enterprise_backend_template.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;

@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();

        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();

            // Load all .properties files inside /i18n/
            Resource[] resources = resolver.getResources("classpath:messages/*.properties");

            // Extract base names without the .properties extension
            String[] baseNames = Arrays.stream(resources)
                    .map(resource -> {
                        try {
                            String path = resource.getURI().toString();
                            return path.substring(0, path.lastIndexOf("."));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(String[]::new);

            messageSource.setBasenames(baseNames);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load message files", e);
        }

        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);

        return messageSource;
    }
}
