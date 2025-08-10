package ru.neoflex.kubrak.gateway.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.neoflex.kubrak.gateway.model.enums.EmploymentPosition;
import ru.neoflex.kubrak.gateway.model.enums.EmploymentStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employment")
public class Employment {

    private EmploymentStatus employmentStatus;
    private String employerInn;
    private BigDecimal salary;
    private EmploymentPosition employmentPosition;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}