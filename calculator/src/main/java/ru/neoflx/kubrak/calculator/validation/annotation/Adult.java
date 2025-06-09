package ru.neoflx.kubrak.calculator.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.neoflx.kubrak.calculator.validation.validator.AdultValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AdultValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Adult {

    String message() default "Min age must be at least {minAge} years old and max age must be not more than {maxAge}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minAge() default 18;
    int maxAge() default 60;
}
