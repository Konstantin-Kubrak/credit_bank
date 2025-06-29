package ru.neoflex.kubrak.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.kubrak.deal.client.CalculatorClientService;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.repository.ClientRepository;
import ru.neoflex.kubrak.deal.repository.StatementRepository;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private ClientService clientService;

    @Mock
    private StatementService statementService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private CalculatorClientService calculatorClientService;

    @InjectMocks
    private OfferService offerService;

    private LoanStatementRequestDto loanRequest;
    private Client client;
    private Statement statement;
    private List<LoanOfferDto> expectedOffers;

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
        when(statementService.createStatement(client)).thenReturn(statement);
        when(calculatorClientService.getOffers(loanRequest)).thenReturn(expectedOffers);

        List<LoanOfferDto> result = offerService.getLoanOfferList(loanRequest);

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
        when(calculatorClientService.getOffers(loanRequest))
                .thenThrow(new CalculatorServiceException("Calculator error"));

        assertThrows(CalculatorServiceException.class, () ->
                offerService.getLoanOfferList(loanRequest));

        verify(clientRepository).save(client);
        verify(statementRepository).save(statement);
    }
    @Test
    void getLoanOfferList_ShouldHandleEmptyOffersList() {

        when(clientService.createClient(loanRequest)).thenReturn(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        when(calculatorClientService.getOffers(loanRequest)).thenReturn(List.of());

        List<LoanOfferDto> result = offerService.getLoanOfferList(loanRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientRepository).save(client);
        verify(statementRepository).save(statement);
    }
}