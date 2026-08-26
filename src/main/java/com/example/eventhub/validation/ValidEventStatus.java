package com.example.eventhub.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EventStatusValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidEventStatus {
    String message() default "";
    Class<?>[] groups() default {};
    Class<?extends Payload>[] payload() default {};
}
