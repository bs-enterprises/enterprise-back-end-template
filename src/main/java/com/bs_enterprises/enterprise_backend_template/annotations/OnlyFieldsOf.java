package com.bs_enterprises.enterprise_backend_template.annotations;


import com.bs_enterprises.enterprise_backend_template.annotations.classes.OnlyFieldsOfValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OnlyFieldsOfValidator.class)
@Documented
public @interface OnlyFieldsOf {
    String message() default "Payload contains unsupported fields";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    Class<?> value(); // Target class to validate against
}
