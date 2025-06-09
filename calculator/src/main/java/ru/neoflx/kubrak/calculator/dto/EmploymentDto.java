package ru.neoflx.kubrak.calculator.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.neoflx.kubrak.calculator.model.enums.EmploymentStatus;
import ru.neoflx.kubrak.calculator.model.enums.Position;

import java.math.BigDecimal;

@Data
public class EmploymentDto {

    @NotNull(message = "Employment status cannot be null")
    private EmploymentStatus employmentStatus;

    @NotNull(message = "Employer INN cannot be null")
    @Pattern(regexp = "^(\\d{10}|\\d{12})$", message = "INN must be 10 or 12 digits")
    private String employerINN;

    @NotNull(message = "Salary cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    @Digits(integer = 10, fraction = 2, message = "Salary must have up to 10 integer and 2 fraction digits")
    private BigDecimal salary;

    @NotNull(message = "Job position cannot be null")
    private Position position;

    @NotNull(message = "Total work experience cannot be null")
    @Min(value = 0, message = "Total work experience cannot be negative")
    @Max(value = 960, message = "Total work experience cannot be more than 960 months")
    private Integer workExperienceTotal;

    @NotNull(message = "Current work experience cannot be null")
    @Min(value = 0, message = "Current work experience cannot be negative")
    private Integer workExperienceCurrent;
}
