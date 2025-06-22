package ru.neoflex.kubrak.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
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
import ru.neoflex.kubrak.deal.model.jsonb.PaymentScheduleElement;
import ru.neoflex.kubrak.deal.repository.CreditRepository;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private CreditService creditService;

    private final String calcCreditUrl = "/api/calculator/calc";

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {

        ReflectionTestUtils.setField(creditService, "calcCreditUrl", calcCreditUrl);
        Mockito.reset(restClient, requestBodyUriSpec, responseSpec);
    }

    @Test
    void finishCreditRegistration_ShouldCompleteSuccessfully() throws StatementNotFoundException, CreditRequestFailedException {

        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto frrDto = EntityFactory.createTestFinishRegistrationRequestDto();
        Statement statement = EntityFactory.createTestStatement(statementId);
        Client client = statement.getClient();
        CreditDto creditDto = EntityFactory.createTestCreditDto();
        Credit credit = EntityFactory.createTestCredit();

        when(statementService.getStatement(statementId)).thenReturn(statement);
        when(clientService.completeClientData(client, frrDto)).thenReturn(client);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(ScoringDataDto.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CreditDto.class)).thenReturn(creditDto);
        when(creditMapper.toEntity(creditDto)).thenReturn(credit);

        creditService.finishCreditRegistration(statementId, frrDto);

        verify(statementService).getStatement(statementId);
        verify(clientService).completeClientData(client, frrDto);
        verify(creditRepository).save(credit);
        verify(statementService).updateStatement(statement, ApplicationStatus.CC_APPROVED, ChangeType.AUTOMATIC);
        assertEquals(CreditStatus.CALCULATED, credit.getCreditStatus());
        assertEquals(credit, statement.getCredit());
    }

    @Test
    void finishCreditRegistration_ShouldHandlePaymentSchedule() throws StatementNotFoundException, CreditRequestFailedException {

        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto frrDto = EntityFactory.createTestFinishRegistrationRequestDto();
        Statement statement = EntityFactory.createTestStatement(statementId);
        CreditDto creditDto = EntityFactory.createTestCreditDto();
        Credit credit = EntityFactory.createTestCredit();
        credit.setPaymentSchedule(List.of(EntityFactory.createPaymentScheduleElement()));

        when(statementService.getStatement(statementId)).thenReturn(statement);
        when(clientService.completeClientData(any(), any())).thenReturn(statement.getClient());
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(calcCreditUrl)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(ScoringDataDto.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CreditDto.class)).thenReturn(creditDto);
        when(creditMapper.toEntity(creditDto)).thenReturn(credit);

        creditService.finishCreditRegistration(statementId, frrDto);

        assertNotNull(credit.getPaymentSchedule());
        assertEquals(1, credit.getPaymentSchedule().size());
        PaymentScheduleElement payment = credit.getPaymentSchedule().getFirst();
        assertEquals(1, payment.getNumber());
        assertEquals(BigDecimal.valueOf(10000), payment.getTotalPayment());
        assertEquals(BigDecimal.valueOf(2000), payment.getInterestPayment());
        assertEquals(BigDecimal.valueOf(8000), payment.getDebtPayment());
        assertEquals(BigDecimal.valueOf(92000), payment.getRemainingDebt());
    }


    @Test
    void finishCreditRegistration_ShouldThrowWhenStatementNotFound() throws StatementNotFoundException {

        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto frrDto = EntityFactory.createTestFinishRegistrationRequestDto();

        when(statementService.getStatement(statementId)).thenThrow(new StatementNotFoundException(statementId));

        assertThrows(StatementNotFoundException.class, () ->
                creditService.finishCreditRegistration(statementId, frrDto));
    }

    @Test
    void finishCreditRegistration_ShouldThrowWhenCreditCalculationFails() throws StatementNotFoundException {

        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto frrDto = EntityFactory.createTestFinishRegistrationRequestDto();
        Statement statement = EntityFactory.createTestStatement(statementId);
        when(statementService.getStatement(statementId)).thenReturn(statement);
        when(clientService.completeClientData(any(),any())).thenReturn(statement.getClient());
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(ScoringDataDto.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CreditDto.class)).thenReturn(null);

        assertThrows(CreditRequestFailedException.class, () ->
                creditService.finishCreditRegistration(statementId, frrDto));
    }

    @Test
    void createScoringDataDto_ShouldMapAllFieldsCorrectly() {

        Statement statement = EntityFactory.createTestStatement(UUID.randomUUID());
        EmploymentDto employmentDto = new EmploymentDto();

        when(employmentMapper.toDto(statement.getClient().getEmployment())).thenReturn(employmentDto);

        ScoringDataDto result = creditService.createScoringDataDto(statement);

        assertNotNull(result);
        assertEquals(statement.getCredit().getAmount(), result.getAmount());
        assertEquals(statement.getCredit().getTerm(), result.getTerm());
        assertEquals(statement.getClient().getFirstName(), result.getFirstName());
        assertEquals(statement.getClient().getLastName(), result.getLastName());
        assertEquals(statement.getClient().getMiddleName(), result.getMiddleName());
        assertEquals(statement.getClient().getGender(), result.getGender());
        assertEquals(statement.getClient().getBirthDate(), result.getBirthdate());
        assertEquals(statement.getClient().getPassport().getSeries(), result.getPassportSeries());
        assertEquals(statement.getClient().getPassport().getNumber(), result.getPassportNumber());
        assertEquals(statement.getClient().getPassport().getIssueDate(), result.getPassportIssueDate());
        assertEquals(statement.getClient().getPassport().getIssueBranch(), result.getPassportIssueBranch());
        assertEquals(statement.getClient().getMaritalStatus(), result.getMaritalStatus());
        assertEquals(statement.getClient().getDependentAmount(), result.getDependentAmount());
        assertEquals(employmentDto, result.getEmployment());
        assertEquals(statement.getClient().getAccountNumber(), result.getAccountNumber());
        assertEquals(statement.getCredit().getInsuranceEnabled(), result.getIsInsuranceEnabled());
        assertEquals(statement.getCredit().getSalaryClient(), result.getIsSalaryClient());
    }

    @Test
    void getCreditFromCalcService_ShouldReturnCreditDto() throws CreditRequestFailedException {

        ScoringDataDto scoringData = new ScoringDataDto();
        CreditDto expected = EntityFactory.createTestCreditDto();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(scoringData)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CreditDto.class)).thenReturn(expected);

        CreditDto result = creditService.getCreditFromCalcService(scoringData);

        assertEquals(expected, result);
    }

    @Test
    void getCreditFromCalcService_ShouldThrowWhenResponseIsNull() {

        ScoringDataDto scoringData = new ScoringDataDto();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(scoringData)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CreditDto.class)).thenReturn(null);

        assertThrows(CreditRequestFailedException.class, () ->
                creditService.getCreditFromCalcService(scoringData));
    }

    @Test
    void saveCredit_ShouldCallRepositorySave() {

        Credit credit = EntityFactory.createTestCredit();
        creditService.saveCredit(credit);
        verify(creditRepository).save(credit);
    }


}