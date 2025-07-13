package ru.neoflex.kubrak.statement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.statement.dto.LoanOfferDto;
import ru.neoflex.kubrak.statement.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.statement.exception.PreScoringException;
import ru.neoflex.kubrak.statement.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    @Value("${validation.credit-data.pattern.name}")
    private String name_pattern;
    @Value("${validation.credit-data.pattern.passport.series}")
    private String passport_series_pattern;
    @Value("${validation.credit-data.pattern.passport.number}")
    private String passport_number_pattern;
    @Value("${validation.credit-data.pattern.email}")
    private String email_pattern;
    @Value("${validation.credit-data.name.length.min}")
    private int nameMinLength;
    @Value("${validation.credit-data.name.length.max}")
    private int nameMaxLength;
    @Value("${validation.credit-data.amount.min}")
    private double amountMinimum;
    @Value("${validation.credit-data.term.min}")
    private int termMinimum;
    @Value("${validation.credit-data.term.max}")
    private int termMaximum;
    @Value("${validation.credit-data.age.min}")
    private int ageMinimum;


    public void validateLoanOffer(LoanOfferDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("LoanOfferDto cannot be null");
        }
        if (dto.getStatementId() != null) {
            validateNotBlank(dto.getStatementId().toString(), "Statement ID");
        }
        else throw new ValidationException("Statement ID cannot be null");
        validatePositiveDecimal(dto.getRequestedAmount(), "Requested amount");
        validatePositiveDecimal(dto.getTotalAmount(), "Total amount");
        validatePositiveNumber(dto.getTerm(), "Term");
        validatePositiveDecimal(dto.getMonthlyPayment(), "Monthly payment");
        validatePositiveDecimal(dto.getRate(), "Rate");
        validateNotNull(dto.getIsInsuranceEnabled(), "Insurance enabled");
        validateNotNull(dto.getIsSalaryClient(), "Salary client");
    }

    public void preScoring(LoanStatementRequestDto lsrDto){

        validateName(lsrDto.getFirstName(), "First name", nameMinLength, nameMaxLength);
        validateName(lsrDto.getLastName(), "Last name", nameMinLength, nameMaxLength);
        if (lsrDto.getMiddleName() != null) {
            validateName(lsrDto.getMiddleName(), "Middle name", nameMinLength, nameMaxLength);
        }

        validateAmount(lsrDto.getAmount(), amountMinimum);
        validateTerm(lsrDto.getTerm(), termMinimum, termMaximum);
        validateEmail(lsrDto.getEmail());
        validateBirthdate(lsrDto.getBirthdate(), ageMinimum);
        validatePassportSeries(lsrDto.getPassportSeries());
        validatePassportNumber(lsrDto.getPassportNumber());
    }


    protected void validateAmount(BigDecimal amount, double minAmount) {
        validateNotNull(amount, "Amount");
        if (amount.compareTo(BigDecimal.valueOf(minAmount)) < 0) {
            throw new PreScoringException("Amount must be at least " + minAmount);
        }
    }

    protected void validateTerm(int term, int minTerm, int maxTerm) {
        if (term < minTerm || term > maxTerm) {
            throw new PreScoringException("Term must be between " + minTerm + " and " + maxTerm + " months");
        }
    }

    protected void validateName(String name, String fieldName, int minLength, int maxLength) {
        validateNotBlank(name, fieldName);
        if (!Pattern.compile(name_pattern).matcher(name).matches()) {
            throw new PreScoringException(fieldName + " must contain only letters");
        }
        if (name.length() < minLength || name.length() > maxLength) {
            throw new PreScoringException(fieldName + " length must be between " + minLength + " and " + maxLength + " characters");
        }
    }

    protected void validateEmail(String email) {
        validateNotBlank(email, "Email");
        if (!Pattern.compile(email_pattern).matcher(email).matches()) {
            throw new PreScoringException("Email should be valid");
        }
    }

    protected void validateBirthdate(LocalDate birthdate, int minAge) {
        validateNotNull(birthdate, "Birthdate");
        LocalDate now = LocalDate.now();
        Period period = Period.between(birthdate, now);
        if (period.getYears() < minAge) {
            throw new PreScoringException("Age must be at least " + minAge + " years old");
        }
    }

    protected void validatePassportSeries(String series) {
        validateNotBlank(series, "Passport series");
        if (!Pattern.compile(passport_series_pattern).matcher(series).matches()) {
            throw new PreScoringException("Passport series must be 4 digits");
        }
    }

    protected void validatePassportNumber(String number) {
        validateNotBlank(number, "Passport number");
        if (!Pattern.compile(passport_number_pattern).matcher(number).matches()) {
            throw new PreScoringException("Passport number must be 6 digits");
        }
    }

    protected void validatePositiveDecimal(BigDecimal value, String fieldName) {
        validateNotNull(value, fieldName);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }

    protected void validatePositiveNumber(Number value, String fieldName) {
        validateNotNull(value, fieldName);
        if (value.doubleValue() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }

    protected void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
    }

    protected void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
    }
}
