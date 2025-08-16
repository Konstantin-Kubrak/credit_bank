package ru.neoflex.kubrak.gateway.dto;


import lombok.Data;
import lombok.experimental.Accessors;
import ru.neoflex.kubrak.gateway.model.enums.EmploymentPosition;
import ru.neoflex.kubrak.gateway.model.enums.EmploymentStatus;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class EmploymentDto {

    private EmploymentStatus employmentStatus;
    private String employerInn;
    private BigDecimal salary;
    private EmploymentPosition employmentPosition;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;

}