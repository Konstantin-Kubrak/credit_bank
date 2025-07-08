package ru.neoflex.kubrak.deal.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.model.jsonb.LoanOffer;
import ru.neoflex.kubrak.deal.model.jsonb.StatusHistory;
import ru.neoflex.kubrak.deal.model.jsonb.converter.LoanOfferConverter;
import ru.neoflex.kubrak.deal.model.jsonb.converter.StatusHistoryConverter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "statement")
public class Statement {

    @Id
    @Column(name = "statement_id", nullable = false, updatable = false)
    @NotNull
    private UUID statementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id", referencedColumnName = "credit_id")
    private Credit credit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private ApplicationStatus status;

    @Column(name = "creation_date", nullable = false)
    @NotNull
    private Timestamp creationDate;

    @Convert(converter = LoanOfferConverter.class)
    @Column(name = "applied_offer", columnDefinition = "jsonb")
    private LoanOffer appliedOffer;

    @Column(name = "sign_date")
    private Timestamp signDate;

    @Column(name = "ses_code")
    private String sesCode;

    @Convert(converter = StatusHistoryConverter.class)
    @Column(name = "status_history", columnDefinition = "jsonb")
    @Builder.Default
    private List<StatusHistory> statusHistory = new ArrayList<>();

    public void addStatusHistory(StatusHistory history) {
        if (statusHistory == null) {
            statusHistory = new ArrayList<>();
        }
        statusHistory.add(history);
    }
}