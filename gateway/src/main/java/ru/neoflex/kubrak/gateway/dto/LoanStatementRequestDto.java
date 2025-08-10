package ru.neoflex.kubrak.gateway.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class LoanStatementRequestDto {

    private BigDecimal amount;
    private Integer term;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate birthdate;
    private String passportSeries;
    private String passportNumber;
}