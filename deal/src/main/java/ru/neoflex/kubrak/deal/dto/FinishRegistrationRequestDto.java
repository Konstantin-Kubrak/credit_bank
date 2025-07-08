package ru.neoflex.kubrak.deal.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.neoflex.kubrak.deal.model.enums.Gender;
import ru.neoflex.kubrak.deal.model.enums.MaritalStatus;

import java.time.LocalDate;
@Data
@Accessors(chain = true)
@Schema(description = "Additional client data for scoring")
public class FinishRegistrationRequestDto {

    @NotNull(message = "Gender cannot be null")
    @Schema(description = "Gender")
    private Gender gender;

    @NotNull(message = "Marital status cannot be null")
    @Schema(description = "Marital status")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Dependent amount cannot be null")
    @Schema(description = "Dependent amount")
    private Integer dependentAmount;

    @NotNull(message = "Passport issue date cannot be null")
    @Schema(description = "Passport issue date")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate passportIssueDate;

    @NotBlank(message = "Passport issue branch cannot be blank")
    @Schema(description = "Passport issue branch")
    private String passportIssueBranch;

    @NotNull(message = "Employment cannot be null")
    @Schema(description = "Данные о трудоустройстве")
    private EmploymentDto employment;

    @NotBlank(message = "Account number cannot be blank")
    @Schema(description = "Account number")
    private String accountNumber;

}
