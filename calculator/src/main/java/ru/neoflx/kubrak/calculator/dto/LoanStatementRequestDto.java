package ru.neoflx.kubrak.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.neoflx.kubrak.calculator.validation.annotation.Adult;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Заявка на получение кредита")
public class LoanStatementRequestDto {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Schema(description = "Запрашиваемая сумма", example = "100000")
    private BigDecimal amount;

    @NotNull(message = "Term cannot be null")
    @Min(value = 1, message = "Term must be at least 1")
    @Max(value = 60, message = "Term cannot be more than 60")
    @Schema(description = "Срок кредита в месяцах", example = "12")
    private Integer term;

    @NotNull(message = "First name cannot be null")
    @Pattern(
            regexp="^[a-zA-Zа-яА-Я]{2,20}$",
            message="Invalid first name, must contain only letters and length must be between 2 and 20 chars")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @Pattern(regexp="^[a-zA-Zа-яА-Я]{2,20}$",
            message="Invalid last name, must contain only letters and length must be between 2 and 20 chars")
    private String lastName;

    @NotNull(message = "Middle name cannot be null")
    @Pattern(regexp="^[a-zA-Zа-яА-Я]{2,20}$",
            message="Invalid middle name, must contain only letters and length must be between 2 and 20 chars")
    private String middleName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Adult(minAge = 18, maxAge = 60, message = "Birthdate cannot be null and age must be between 18 and 60")
    private LocalDate birthdate;

    @NotNull(message = "Passport series cannot be null")
    @Pattern(regexp = "^[0-9]{4}$", message = "Passport series must be 4 digits")
    private String passportSeries;

    @NotNull(message = "Passport number cannot be null")
    @Pattern(regexp = "^[0-9]{6}$", message = "Passport number must be 6 digits")
    private String passportNumber;

}
