package com.bs_enterprises.enterprise_backend_template.annotations.classes;

import com.bs_enterprises.enterprise_backend_template.annotations.OnlyFieldsOf;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OnlyFieldsOfValidator implements ConstraintValidator<OnlyFieldsOf, Map<String, Object>> {

    private Set<String> allowedFields;
    private String baseMessage;

    @Override
    public void initialize(OnlyFieldsOf constraintAnnotation) {
        Class<?> targetClass = constraintAnnotation.value();
        this.baseMessage = constraintAnnotation.message();

        // All valid field names of the target class
        this.allowedFields = Arrays.stream(targetClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Map<String, Object> value, ConstraintValidatorContext context) {
        if (value == null) return true;

        // Collect unsupported fields
        List<String> unsupported = value.keySet().stream()
                .filter(f -> !allowedFields.contains(f))
                .toList();

        if (unsupported.isEmpty()) {
            return true;
        }

        // Disable default message
        context.disableDefaultConstraintViolation();

        // Build detailed message
        String msg = baseMessage + ": " + String.join(", ", unsupported);

        // Add custom message
        context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();

        return false;
    }
}
