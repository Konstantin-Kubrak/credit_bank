package ru.neoflex.kubrak.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.kubrak.deal.client.CalculatorClient;
import ru.neoflex.kubrak.deal.dto.*;
import ru.neoflex.kubrak.deal.dto.dtoMapper.CreditMapper;
import ru.neoflex.kubrak.deal.dto.dtoMapper.EmploymentMapper;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.exception.CreditRequestFailedException;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Credit;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.model.enums.ChangeType;
import ru.neoflex.kubrak.deal.model.enums.CreditStatus;
import ru.neoflex.kubrak.deal.repository.ClientRepository;
import ru.neoflex.kubrak.deal.repository.CreditRepository;
import ru.neoflex.kubrak.deal.repository.StatementRepository;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private ClientService clientService;
    @Mock
    private StatementService statementService;

    @Mock
    DossierService dossierService;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CalculatorClient calculatorClient;

    @Mock
    private EmploymentMapper employmentMapper;
    @Mock
    private CreditMapper creditMapper;

    @InjectMocks
    private DealService dealService;

    private LoanStatementRequestDto loanRequest;
    private Client client;
    private Statement statement;
    private List<LoanOfferDto> expectedOffers;

    @BeforeEach
    void setUp() {

        loanRequest = EntityFactory.createTestLoanRequest();
        statement = EntityFactory.createTestStatement(UUID.randomUUID());
        client = statement.getClient();
        expectedOffers = EntityFactory.createExpectedOffers();
        creditDto = EntityFactory.createTestCreditDto();
        credit = EntityFactory.createTestCredit();
        frrDto = EntityFactory.createTestFinishRegistrationRequestDto();
    }

    private FinishRegistrationRequestDto frrDto;
    private CreditDto creditDto;
    private Credit credit;

    @Test
    void finishCreditRegistration_ShouldCompleteSuccessfully() throws StatementNotFoundException, CreditRequestFailedException {

        when(statementRepository.findById(statement.getStatementId())).thenReturn(Optional.of(statement));
        when(clientService.completeClientData(client, frrDto)).thenReturn(client);
        when(calculatorClient.getCredit(any(ScoringDataDto.class))).thenReturn(creditDto);
        when(creditMapper.toEntity(creditDto)).thenReturn(credit);

        dealService.finishCreditRegistration(statement.getStatementId(), frrDto);

        verify(creditRepository).save(credit);
        verify(statementService).updateStatement(statement, ApplicationStatus.CC_APPROVED, ChangeType.AUTOMATIC);
        assertEquals(CreditStatus.CALCULATED, credit.getCreditStatus());
    }

    @Test
    void finishCreditRegistration_ShouldThrowWhenStatementNotFound() throws StatementNotFoundException {

        UUID statementId = UUID.randomUUID();
        when(statementRepository.findById(statementId)).thenThrow(new StatementNotFoundException(statementId));

        assertThrows(StatementNotFoundException.class, () ->
                dealService.finishCreditRegistration(statementId, frrDto));
    }

    @Test
    void finishCreditRegistration_ShouldThrowWhenCreditCalculationFails() throws StatementNotFoundException, CreditRequestFailedException {

        when(statementRepository.findById(statement.getStatementId())).thenReturn(Optional.of(statement));
        when(clientService.completeClientData(client, frrDto)).thenReturn(client);
        when(calculatorClient.getCredit(any(ScoringDataDto.class)))
                .thenThrow(new CreditRequestFailedException("Calculation failed"));

        assertThrows(CreditRequestFailedException.class, () ->
                dealService.finishCreditRegistration(statement.getStatementId(), frrDto));
    }

    @Test
    void createScoringDataDto_ShouldMapAllFieldsCorrectly() {

        Statement statement = EntityFactory.createTestStatement(UUID.randomUUID());
        when(employmentMapper.toDto(statement.getClient().getEmployment())).thenReturn(new EmploymentDto());

        ScoringDataDto result = dealService.createScoringDataDto(statement);

        assertNotNull(result);
        assertEquals(statement.getCredit().getAmount(), result.getAmount());
        assertEquals(statement.getClient().getFirstName(), result.getFirstName());
        assertEquals(statement.getClient().getLastName(), result.getLastName());
    }


    @Test
    void getLoanOfferList_ShouldReturnOffersWithStatementId() {
        when(clientService.createClient(loanRequest)).thenReturn(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        when(calculatorClient.getOffers(loanRequest)).thenReturn(expectedOffers);

        List<LoanOfferDto> result = dealService.getLoanOfferList(loanRequest);

        assertNotNull(result);
        assertEquals(expectedOffers.size(), result.size());
        result.forEach(offer -> assertEquals(statement.getStatementId(), offer.getStatementId()));
        verify(clientRepository).save(client);
        verify(statementRepository).save(statement);
    }

    @Test
    void getLoanOfferList_ShouldHandleCalculatorServiceError() {
        when(clientService.createClient(loanRequest)).thenReturn(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        when(calculatorClient.getOffers(loanRequest))
                .thenThrow(new CalculatorServiceException("Calculator error"));

        assertThrows(CalculatorServiceException.class, () ->
                dealService.getLoanOfferList(loanRequest));

        verify(clientRepository).save(client);
        verify(statementRepository).save(statement);
    }

    @Test
    void finishCreditRegistration_ShouldThrowWhenCreditIsNull() {

        when(statementRepository.findById(any())).thenReturn(Optional.of(statement));
        when(clientService.completeClientData(any(),any())).thenReturn(client);
        when(calculatorClient.getCredit(any())).thenReturn(null);
        assertThrows(CreditRequestFailedException.class, () ->
                dealService.finishCreditRegistration(UUID.randomUUID(), frrDto));
    }

    @Test
    void getLoanOfferList_ShouldThrowWhenOffersListIsNullOrEmpty() {

        when(clientService.createClient(loanRequest)).thenReturn(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        when(calculatorClient.getOffers(loanRequest)).thenReturn(List.of());

        assertThrows(CalculatorServiceException.class, () ->
                dealService.getLoanOfferList(loanRequest));

        when(calculatorClient.getOffers(loanRequest)).thenReturn(null);

        assertThrows(CalculatorServiceException.class, () ->
                dealService.getLoanOfferList(loanRequest));
    }
}