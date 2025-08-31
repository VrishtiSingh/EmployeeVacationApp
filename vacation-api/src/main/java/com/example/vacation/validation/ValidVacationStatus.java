package com.example.vacation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidVacationStatusValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVacationStatus {
    String message() default "Invalid vacation status";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
