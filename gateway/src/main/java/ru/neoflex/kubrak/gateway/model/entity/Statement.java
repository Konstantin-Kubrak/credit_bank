package ru.neoflex.kubrak.gateway.model.entity;

import lombok.*;
import ru.neoflex.kubrak.gateway.model.enums.ApplicationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Statement {

    private UUID statementId;
    private Client client;
    private Credit credit;
    private ApplicationStatus status;
    private LocalDate creationDate;
    private LoanOffer appliedOffer;
    private LocalDate signDate;
    private String sesCode;
    private List<StatusHistory> statusHistory;
}