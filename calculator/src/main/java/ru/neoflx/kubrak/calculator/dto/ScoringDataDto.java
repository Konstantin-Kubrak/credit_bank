package ru.neoflx.kubrak.calculator.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.neoflx.kubrak.calculator.model.enums.Gender;
import ru.neoflx.kubrak.calculator.model.enums.MaritalStatus;
import ru.neoflx.kubrak.calculator.validation.annotation.Adult;
import ru.neoflx.kubrak.calculator.validation.annotation.MinimumDate;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScoringDataDto {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Term cannot be null")
    @Min(value = 1, message = "Term must be at least 1")
    @Max(value = 60, message = "Term cannot be more than 60")
    private Integer term;

    @NotNull(message = "First name cannot be null")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]{2,20}$",
            message = "Invalid first name, must contain only letters and length must be between 2 and 20 chars")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]{2,20}$",
            message = "Invalid last name, must contain only letters and length must be between 2 and 20 chars")
    private String lastName;

    @NotNull(message = "Middle name cannot be null")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]{2,20}$",
            message = "Invalid middle name, must contain only letters and length must be between 2 and 20 chars")
    private String middleName;

    @NotNull(message = "The gender must be specified.")
    private Gender gender;

    @Adult(minAge = 18, maxAge = 60, message = "Birthdate cannot be null and age must be between 18 and 60")
    private LocalDate birthdate;

    @NotBlank(message = "Passport series cannot be blank")
    @Pattern(regexp = "^[0-9]{4}$", message = "Passport series must be 4 digits")
    private String passportSeries;

    @NotBlank(message = "Passport number cannot be blank")
    @Pattern(regexp = "^[0-9]{6}$", message = "Passport number must be 6 digits")
    private String passportNumber;

    @MinimumDate(message = "RF passport cannot be issued before 1997-10-01")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Passport issue branch cannot be blank")
    private String passportIssueBranch;

    @NotNull(message = "Marital status cannot be null")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Dependent amount cannot be null")
    @PositiveOrZero(message = "Dependent amount must be positive or zero")
    private Integer dependentAmount;

    @NotNull(message = "Employment cannot be null")
    private EmploymentDto employment;

    @NotBlank(message = "Account number cannot be blank")
    private String accountNumber;

    @NotNull(message = "Insurance status must be specified")
    private Boolean isInsuranceEnabled;

    @NotNull(message = "Salary client status must be specified")
    private Boolean isSalaryClient;

}