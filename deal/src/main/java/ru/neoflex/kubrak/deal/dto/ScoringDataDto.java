package ru.neoflex.kubrak.deal.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.neoflex.kubrak.deal.model.enums.Gender;
import ru.neoflex.kubrak.deal.model.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class ScoringDataDto {

    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    @NotNull(message = "Term cannot be null")
    private Integer term;

    @NotNull(message = "First name cannot be null")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    private String lastName;

    private String middleName;

    @NotNull(message = "The gender must be specified")
    private Gender gender;

    @NotNull(message = "Birthdate cannot be null")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate birthdate;

    @NotBlank(message = "Passport series cannot be blank")
    private String passportSeries;

    @NotBlank(message = "Passport number cannot be blank")
    private String passportNumber;

    @NotNull(message = "Passport issie date ")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Passport issue branch cannot be blank")
    private String passportIssueBranch;

    @NotNull(message = "Marital status cannot be null")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Dependent amount cannot be null")
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