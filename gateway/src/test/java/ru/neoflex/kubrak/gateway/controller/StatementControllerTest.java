package ru.neoflex.kubrak.gateway.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.neoflex.kubrak.gateway.client.StatementClient;
import ru.neoflex.kubrak.gateway.dto.LoanOfferDto;
import ru.neoflex.kubrak.gateway.dto.LoanStatementRequestDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class StatementControllerTest {

    @Mock
    private StatementClient statementClient;

    @InjectMocks
    private StatementController statementController;

    @Test
    void submitApplication_ShouldReturnOffers() {

        LoanStatementRequestDto request = new LoanStatementRequestDto();
        List<LoanOfferDto> expectedOffers = List.of(new LoanOfferDto());
        when(statementClient.getLoanOffers(any())).thenReturn(expectedOffers);

        ResponseEntity<List<LoanOfferDto>> response = statementController.submitApplication(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedOffers, response.getBody());
        verify(statementClient, times(1)).getLoanOffers(request);
    }

    @Test
    void selectOffer_ShouldReturnNoContent() {

        LoanOfferDto offer = new LoanOfferDto();

        ResponseEntity<Void> response = statementController.selectOffer(offer);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(statementClient, times(1)).selectOffer(offer);
    }
}