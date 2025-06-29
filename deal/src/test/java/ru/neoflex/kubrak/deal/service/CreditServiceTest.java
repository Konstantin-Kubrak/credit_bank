package ru.neoflex.kubrak.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.kubrak.deal.client.CalculatorClientService;
import ru.neoflex.kubrak.deal.dto.CreditDto;
import ru.neoflex.kubrak.deal.dto.EmploymentDto;
import ru.neoflex.kubrak.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.kubrak.deal.dto.ScoringDataDto;
import ru.neoflex.kubrak.deal.dto.dtoMapper.CreditMapper;
import ru.neoflex.kubrak.deal.dto.dtoMapper.EmploymentMapper;
import ru.neoflex.kubrak.deal.exception.CreditRequestFailedException;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Credit;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.model.enums.ChangeType;
import ru.neoflex.kubrak.deal.model.enums.CreditStatus;
import ru.neoflex.kubrak.deal.repository.CreditRepository;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private StatementService statementService;
    @Mock
    private ClientService clientService;
    @Mock
    private EmploymentMapper employmentMapper;
    @Mock
    private CreditMapper creditMapper;
    @Mock
    private CreditRepository creditRepository;
    @Mock
    private CalculatorClientService calculatorClientService;

    @InjectMocks
    private CreditService creditService;

    private FinishRegistrationRequestDto frrDto;
    private Statement statement;
    private Client client;
    private CreditDto creditDto;
    private Credit credit;

    @BeforeEach
    void setUp() {

        frrDto = EntityFactory.createTestFinishRegistrationRequestDto();
        statement = EntityFactory.createTestStatement(UUID.randomUUID());
        client = statement.getClient();
        creditDto = EntityFactory.createTestCreditDto();
        credit = EntityFactory.createTestCredit();
    }

    @Test
    void finishCreditRegistration_ShouldCompleteSuccessfully() throws StatementNotFoundException, CreditRequestFailedException {

        when(statementService.getStatement(statement.getStatementId())).thenReturn(statement);
        when(clientService.completeClientData(client, frrDto)).thenReturn(client);
        when(calculatorClientService.getCredit(any(ScoringDataDto.class))).thenReturn(creditDto);
        when(creditMapper.toEntity(creditDto)).thenReturn(credit);

        creditService.finishCreditRegistration(statement.getStatementId(), frrDto);

        verify(creditRepository).save(credit);
        verify(statementService).updateStatement(statement, ApplicationStatus.CC_APPROVED, ChangeType.AUTOMATIC);
        assertEquals(CreditStatus.CALCULATED, credit.getCreditStatus());
    }

    @Test
    void finishCreditRegistration_ShouldThrowWhenStatementNotFound() throws StatementNotFoundException {

        UUID statementId = UUID.randomUUID();
        when(statementService.getStatement(statementId)).thenThrow(new StatementNotFoundException(statementId));

        assertThrows(StatementNotFoundException.class, () ->
                creditService.finishCreditRegistration(statementId, frrDto));
    }

    @Test
    void finishCreditRegistration_ShouldThrowWhenCreditCalculationFails() throws StatementNotFoundException, CreditRequestFailedException {

        when(statementService.getStatement(statement.getStatementId())).thenReturn(statement);
        when(clientService.completeClientData(client, frrDto)).thenReturn(client);
        when(calculatorClientService.getCredit(any(ScoringDataDto.class)))
                .thenThrow(new CreditRequestFailedException("Calculation failed"));

        assertThrows(CreditRequestFailedException.class, () ->
                creditService.finishCreditRegistration(statement.getStatementId(), frrDto));
    }

    @Test
    void createScoringDataDto_ShouldMapAllFieldsCorrectly() {

        Statement statement = EntityFactory.createTestStatement(UUID.randomUUID());
        when(employmentMapper.toDto(statement.getClient().getEmployment())).thenReturn(new EmploymentDto());

        ScoringDataDto result = creditService.createScoringDataDto(statement);

        assertNotNull(result);
        assertEquals(statement.getCredit().getAmount(), result.getAmount());
        assertEquals(statement.getClient().getFirstName(), result.getFirstName());
        assertEquals(statement.getClient().getLastName(), result.getLastName());
    }
}