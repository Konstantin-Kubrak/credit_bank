package ru.neoflex.kubrak.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.neoflex.kubrak.calculator.validation.annotation.Adult;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
@Schema(description = "Loan request")
public class LoanStatementRequestDto {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "20000.0", message = "Amount must be at least 20000")
    @Schema(description = "Запрашиваемая сумма", example = "100000")
    private BigDecimal amount;

    @NotNull(message = "Term cannot be null")
    @Min(value = 6, message = "Term must be at least 6")
    @Max(value = 60, message = "Term cannot be more than 60")
    @Schema(description = "Срок кредита в месяцах", example = "12")
    private Integer term;

    @NotNull(message = "First name cannot be null")
    @Pattern(
            regexp="^[a-zA-Zа-яА-Я]{2,30}$",
            message="Invalid first name, must contain only letters and length must be between 2 and 30 chars")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @Pattern(regexp="^[a-zA-Zа-яА-Я]{2,30}$",
            message="Invalid last name, must contain only letters and length must be between 2 and 30 chars")
    private String lastName;

    @Pattern(regexp="^[a-zA-Zа-яА-Я]{2,30}$",
            message="Invalid middle name, must contain only letters and length must be between 2 and 30 chars")
    private String middleName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Adult(minAge = 20, maxAge = 65, message = "Birthdate cannot be null and age must be between 20 and 65")
    private LocalDate birthdate;

    @NotNull(message = "Passport series cannot be null")
    @Pattern(regexp = "^[0-9]{4}$", message = "Passport series must be 4 digits")
    private String passportSeries;

    @NotNull(message = "Passport number cannot be null")
    @Pattern(regexp = "^[0-9]{6}$", message = "Passport number must be 6 digits")
    private String passportNumber;
}