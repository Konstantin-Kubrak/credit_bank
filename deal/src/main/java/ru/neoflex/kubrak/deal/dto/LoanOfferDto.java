package ru.neoflex.kubrak.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(description = "Loan offer")
public class LoanOfferDto {

    @NotNull(message = "Statement ID cannot be null")
    private UUID statementId;

    @NotNull(message = "Requested amount cannot be null")
    private BigDecimal requestedAmount;

    @NotNull(message = "Total amount cannot be null")
    private BigDecimal totalAmount;

    @NotNull(message = "Term cannot be null")
    private Integer term;

    @NotNull(message = "Monthly payment cannot be null")
    private BigDecimal monthlyPayment;

    @NotNull(message = "Rate cannot be null")
    private BigDecimal rate;

    @NotNull(message = "Insurance enabled flag cannot be null")
    private Boolean isInsuranceEnabled;

    @NotNull(message = "Salary client flag cannot be null")
    private Boolean isSalaryClient;

}