package ru.neoflex.kubrak.calculator.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.neoflex.kubrak.calculator.validation.annotation.Adult;

import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(Adult constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
        this.maxAge = constraintAnnotation.maxAge();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {

        if (birthDate == null) {
            return false;
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return  (age >= 18 && age < 60);
    }
}
