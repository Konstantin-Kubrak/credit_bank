package ru.neoflex.kubrak.calculator.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.neoflex.kubrak.calculator.validation.annotation.MinimumDate;

import java.time.LocalDate;

public class MinimumDateValidator implements ConstraintValidator<MinimumDate, LocalDate> {

    private LocalDate minimumDate;

    @Override
    public void initialize(MinimumDate constraintAnnotation) {
        minimumDate = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {

        if(value == null)
            return false;

        return !value.isBefore(minimumDate);
    }
}