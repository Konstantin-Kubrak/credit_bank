package ru.neoflex.kubrak.statement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.kubrak.statement.client.DealClient;
import ru.neoflex.kubrak.statement.dto.LoanOfferDto;
import ru.neoflex.kubrak.statement.dto.LoanStatementRequestDto;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private DealClient dealClient;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private StatementService statementService;

    @Test
    void getLoanOffers_ValidRequest_ReturnsOffers() {
        LoanStatementRequestDto request = LoanStatementRequestDto.builder().build();
        List<LoanOfferDto> expectedOffers = List.of(new LoanOfferDto(), new LoanOfferDto());

        when(dealClient.getOffers(request)).thenReturn(expectedOffers);

        List<LoanOfferDto> result = statementService.getLoanOffers(request);

        assertEquals(2, result.size());
        verify(validationService).preScoring(request);
        verify(dealClient).getOffers(request);
    }

    @Test
    void selectOffer_ValidOffer_CallsDealClient() {
        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(UUID.randomUUID());

        assertDoesNotThrow(() -> statementService.selectOffer(offer));
        verify(validationService).validateLoanOffer(offer);
        verify(dealClient).selectOfferUrl(offer);
    }
}