package ru.neoflex.kubrak.deal.model.jsonb;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class Passport {

    private String series;
    private String number;
    private String issueBranch;
    private LocalDate issueDate;
}
