package ru.neoflex.kubrak.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.neoflex.kubrak.deal.model.enums.EmploymentPosition;
import ru.neoflex.kubrak.deal.model.enums.EmploymentStatus;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@Schema(description = "Данные о трудоустройстве клиента")
public class EmploymentDto {

    @NotNull(message = "Employment status cannot be null")
    @Schema(description = "Статус занятости")
    private EmploymentStatus employmentStatus;

    @NotBlank(message = "Employer INN cannot be blank")
    @Schema(description = "ИНН работодателя",
            required = true,
            example = "1234567890")
    private String employerInn;

    @NotNull(message = "Salary cannot be null")
    @Schema(description = "Employee salary",
            required = true,
            example = "100000.00")
    private BigDecimal salary;

    @NotNull(message = "Employment position cannot be null")
    @Schema(description = "Employee job position",
            required = true)
    private EmploymentPosition employmentPosition;

    @NotNull(message = "Total work experience cannot be null")
    @Schema(description = "Total work experience in months",
            required = true,
            example = "60")
    private Integer workExperienceTotal;

    @NotNull(message = "Current work experience cannot be null")
    @Schema(description = "Total work experience in months at current job",
            required = true,
            example = "24")
    private Integer workExperienceCurrent;

}