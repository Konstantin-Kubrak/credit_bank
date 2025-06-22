package ru.neoflex.kubrak.deal.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
@Schema(description = "Loan request")
public class LoanStatementRequestDto {

    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    @NotNull(message = "Term cannot be null")
    private Integer term;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    private String middleName;

    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotNull(message = "Birthdate cannot be null")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate birthdate;

    @NotBlank(message = "Passport series cannot be blank")
    private String passportSeries;

    @NotBlank(message = "Passport number cannot be blank")
    private String passportNumber;

}