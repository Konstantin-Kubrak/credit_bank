package ru.neoflex.kubrak.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.dtoMapper.LoanOfferMapper;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.model.enums.ChangeType;
import ru.neoflex.kubrak.deal.model.jsonb.StatusHistory;
import ru.neoflex.kubrak.deal.repository.StatementRepository;

import java.sql.Timestamp;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatementService {

    private final StatementRepository statementRepository;
    private final LoanOfferMapper loanOfferMapper;

    @Transactional
    public void setStatementLoanOffer(LoanOfferDto loanOfferDto){

        log.info("Setting loan offer for statement ID: {}", loanOfferDto.getStatementId());
        log.debug("Offer details: {}", loanOfferDto);

        Statement statement = statementRepository.findByStatementId(loanOfferDto.getStatementId())
                .orElseThrow(() -> new StatementNotFoundException(loanOfferDto.getStatementId()));
        log.debug("Retrieved statement: {}", statement);

        statement.setStatus(ApplicationStatus.APPROVED);
        StatusHistory statusHistory = createStatusHistory(ApplicationStatus.APPROVED, ChangeType.AUTOMATIC);
        statement.addStatusHistory(statusHistory);
        statement.setAppliedOffer(loanOfferMapper.toEntity(loanOfferDto));
        statementRepository.save(statement);
        log.info("Loan offer successfully set for statement ID: {}", statement.getStatementId());
    }

    public Statement createStatement(Client client) {

        log.debug("Creating new statement for client ID: {}", client.getClientId());
        Statement statement = Statement
                .builder()
                .statementId(UUID.randomUUID())
                .client(client)
                .status(ApplicationStatus.PREAPPROVAL)
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .build();
        log.debug("Statement created: {}", statement);
        return statement;
    }

    public StatusHistory createStatusHistory(ApplicationStatus status, ChangeType changeType){

        return StatusHistory.builder()
                .status(status)
                .time(new Timestamp(System.currentTimeMillis()))
                .changeType(changeType)
                .build();
    }

    public void updateStatement(Statement statement, ApplicationStatus applicationStatus, ChangeType changeType) {

        statement.setStatus(applicationStatus);
        StatusHistory statusHistory = createStatusHistory(applicationStatus, changeType);
        statement.addStatusHistory(statusHistory);

        statementRepository.save(statement);
    }
}
