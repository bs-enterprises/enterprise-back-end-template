package com.bs_enterprises.enterprise_backend_template.services.impl;

import com.bs_enterprises.enterprise_backend_template.services.common.MessageUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageUtilsImpl implements MessageUtils {

    private final MessageSource messageSource;

    public MessageUtilsImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessageFromCode(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

}