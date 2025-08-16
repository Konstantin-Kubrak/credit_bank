package ru.neoflex.kubrak.gateway.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Passport {

    private String series;
    private String number;
    private String issueBranch;
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate issueDate;
}
