package ru.neoflex.kubrak.calculator.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.constraints.Past;
import ru.neoflex.kubrak.calculator.validation.validator.MinimumDateValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumDateValidator.class)
@Past
public @interface MinimumDate {
    String message() default "Date must not be before {value}";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};

    String value() default "1997-10-01";
}

