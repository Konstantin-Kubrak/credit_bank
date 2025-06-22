package ru.neoflex.kubrak.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private ClientService clientService;

    @Mock
    private StatementService statementService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private OfferService offerService;

    private LoanStatementRequestDto loanRequest;
    private Client client;
    private Statement statement;
    private List<LoanOfferDto> expectedOffers;
    private final String calcCreditUrl = "/calculator/offers";

    @BeforeEach
    void setUp() {

        loanRequest = EntityFactory.createLoanRequest();
        client = EntityFactory.createClient();
        statement = EntityFactory.createTestStatement(UUID.randomUUID());
        expectedOffers = EntityFactory.createExpectedOffers();
    }

    @Test
    void getLoanOfferList_ShouldReturnOffersWithStatementId() {

        when(clientService.createClient(loanRequest)).thenReturn(client);
        doNothing().when(clientService).saveClient(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        doNothing().when(statementService).saveStatement(statement);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(calcCreditUrl)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(loanRequest)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(new ParameterizedTypeReference<List<LoanOfferDto>>() {}))
                .thenReturn(expectedOffers);

        List<LoanOfferDto> result = offerService.getLoanOfferList(loanRequest);

        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(offer -> assertEquals(statement.getStatementId(), offer.getStatementId()));
        verify(clientService).createClient(loanRequest);
        verify(clientService).saveClient(client);
        verify(statementService).createStatement(client);
        verify(statementService).saveStatement(statement);
    }

    @Test
    void getLoanOfferList_ShouldHandleCalculatorServiceError() {

        when(clientService.createClient(loanRequest)).thenReturn(client);
        doNothing().when(clientService).saveClient(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        doNothing().when(statementService).saveStatement(statement);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(calcCreditUrl)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(loanRequest)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(new ParameterizedTypeReference<List<LoanOfferDto>>() {}))
                .thenThrow(new CalculatorServiceException("Calculator service error"));

        assertThrows(CalculatorServiceException.class, () ->
                offerService.getLoanOfferList(loanRequest));

        verify(clientService).createClient(loanRequest);
        verify(clientService).saveClient(client);
        verify(statementService).createStatement(client);
        verify(statementService).saveStatement(statement);
    }

    @Test
    void getLoanOfferList_ShouldHandleRestClientException() {
        when(clientService.createClient(loanRequest)).thenReturn(client);
        doNothing().when(clientService).saveClient(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        doNothing().when(statementService).saveStatement(statement);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(calcCreditUrl)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(loanRequest)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RestClientException("Connection error"));

        assertThrows(CalculatorServiceException.class, () ->
                offerService.getLoanOfferList(loanRequest));

        verify(clientService).createClient(loanRequest);
        verify(clientService).saveClient(client);
        verify(statementService).createStatement(client);
        verify(statementService).saveStatement(statement);
    }

    @Test
    void getOffersFromCalculator_ShouldReturnOffers() {

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(calcCreditUrl)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(loanRequest)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(new ParameterizedTypeReference<List<LoanOfferDto>>() {}))
                .thenReturn(expectedOffers);

        List<LoanOfferDto> result = offerService.getOffersFromCalculator(loanRequest);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(requestBodyUriSpec).uri(calcCreditUrl);
    }

    @Test
    void getOffersFromCalculator_ShouldThrowOnErrorResponse() {

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(calcCreditUrl)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(loanRequest)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(new ParameterizedTypeReference<List<LoanOfferDto>>() {}))
                .thenThrow(new CalculatorServiceException("Error response"));

        assertThrows(CalculatorServiceException.class, () ->
                offerService.getOffersFromCalculator(loanRequest));

        verify(requestBodyUriSpec).uri(calcCreditUrl);
    }
}