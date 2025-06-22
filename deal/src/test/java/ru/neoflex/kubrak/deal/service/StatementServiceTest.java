package ru.neoflex.kubrak.deal.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.dtoMapper.LoanOfferMapper;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.model.enums.ChangeType;
import ru.neoflex.kubrak.deal.model.jsonb.LoanOffer;
import ru.neoflex.kubrak.deal.model.jsonb.StatusHistory;
import ru.neoflex.kubrak.deal.repository.StatementRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private LoanOfferMapper loanOfferMapper;

    @InjectMocks
    private StatementService statementService;

    @Test
    void setStatementLoanOffer_ShouldUpdateStatementSuccessfully() throws StatementNotFoundException {

        UUID statementId = UUID.randomUUID();
        LoanOfferDto loanOfferDto = new LoanOfferDto()
                .setStatementId(statementId)
                .setRequestedAmount(BigDecimal.valueOf(100000))
                .setTotalAmount(BigDecimal.valueOf(120000))
                .setTerm(12)
                .setMonthlyPayment(BigDecimal.valueOf(10000))
                .setRate(BigDecimal.valueOf(10))
                .setIsInsuranceEnabled(true)
                .setIsSalaryClient(false);

        Statement existingStatement = Statement.builder()
                .statementId(statementId)
                .status(ApplicationStatus.PREAPPROVAL)
                .build();

        LoanOffer mappedOffer = LoanOffer.builder()
                .statementId(statementId)
                .requestedAmount(loanOfferDto.getRequestedAmount())
                .totalAmount(loanOfferDto.getTotalAmount())
                .term(loanOfferDto.getTerm())
                .monthlyPayment(loanOfferDto.getMonthlyPayment())
                .rate(loanOfferDto.getRate())
                .isInsuranceEnabled(loanOfferDto.getIsInsuranceEnabled())
                .isSalaryClient(loanOfferDto.getIsSalaryClient())
                .build();

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(existingStatement));
        when(loanOfferMapper.toEntity(loanOfferDto)).thenReturn(mappedOffer);

        statementService.setStatementLoanOffer(loanOfferDto);

        assertEquals(ApplicationStatus.APPROVED, existingStatement.getStatus());
        assertEquals(mappedOffer, existingStatement.getAppliedOffer());
        assertEquals(1, existingStatement.getStatusHistory().size());
        verify(statementRepository).save(existingStatement);
    }

    @Test
    void createStatement_ShouldSetCorrectDefaults() {

        Client client = new Client();
        client.setClientId(UUID.randomUUID());

        Statement result = statementService.createStatement(client);

        assertNotNull(result.getStatementId());
        assertEquals(client, result.getClient());
        assertEquals(ApplicationStatus.PREAPPROVAL, result.getStatus());
        assertNotNull(result.getCreationDate());
        assertNull(result.getAppliedOffer());
        assertTrue(result.getStatusHistory().isEmpty());
    }

    @Test
    void createStatusHistory_ShouldCreateValidEntry() {

        StatusHistory history = statementService.createStatusHistory(
                ApplicationStatus.APPROVED,
                ChangeType.AUTOMATIC
        );

        assertEquals(ApplicationStatus.APPROVED, history.getStatus());
        assertEquals(ChangeType.AUTOMATIC, history.getChangeType());
        assertNotNull(history.getTime());
    }

    @Test
    void updateStatement_ShouldUpdateStatusAndHistory() {

        Statement statement = Statement.builder()
                .statementId(UUID.randomUUID())
                .status(ApplicationStatus.PREAPPROVAL)
                .build();

        statementService.updateStatement(
                statement,
                ApplicationStatus.APPROVED,
                ChangeType.MANUAL
        );

        assertEquals(ApplicationStatus.APPROVED, statement.getStatus());
        assertEquals(1, statement.getStatusHistory().size());
        assertEquals(ChangeType.MANUAL, statement.getStatusHistory().getFirst().getChangeType());
        verify(statementRepository).save(statement);
    }

    @Test
    void getStatement_ShouldReturnStatementWhenExists() throws StatementNotFoundException {

        UUID statementId = UUID.randomUUID();
        Statement expected = Statement.builder()
                .statementId(statementId)
                .build();

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(expected));

        Statement result = statementService.getStatement(statementId);

        assertEquals(expected, result);
    }

    @Test
    void getStatement_ShouldThrowWhenNotFound() {

        UUID statementId = UUID.randomUUID();
        when(statementRepository.findById(statementId)).thenReturn(Optional.empty());

        assertThrows(StatementNotFoundException.class, () ->
                statementService.getStatement(statementId));
    }

    @Test
    void saveStatement_ShouldCallRepository() {

        Statement statement = Statement.builder().build();
        statementService.saveStatement(statement);
        verify(statementRepository).save(statement);
    }
}