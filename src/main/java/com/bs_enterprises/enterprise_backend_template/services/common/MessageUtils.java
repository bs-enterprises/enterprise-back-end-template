package com.bs_enterprises.enterprise_backend_template.services.common;

import org.springframework.stereotype.Service;

@Service
public interface MessageUtils {

    String getMessageFromCode(String key, Object... args);

}