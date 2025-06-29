package ru.neoflex.kubrak.deal.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.neoflex.kubrak.deal.dto.CreditDto;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.dto.ScoringDataDto;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.exception.CreditRequestFailedException;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculatorClientServiceTest {

    private static final String TEST_OFFERS_URL = "/calculator/offers";
    private static final String TEST_CREDIT_URL = "/calculator/calc";

    @Mock private RestClient restClient;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private CalculatorClientService calculatorClientService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        calculatorClientService = new CalculatorClientService(restClient);

        setPrivateField(calculatorClientService, "offersUrl", TEST_OFFERS_URL);
        setPrivateField(calculatorClientService, "calcCreditUrl", TEST_CREDIT_URL);
    }

    private void setPrivateField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void getOffers_ShouldReturnOffers(){

        LoanStatementRequestDto request = EntityFactory.createLoanRequest();
        List<LoanOfferDto> expected = EntityFactory.createExpectedOffers();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TEST_OFFERS_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        List<LoanOfferDto> result = calculatorClientService.getOffers(request);

        assertEquals(expected, result);
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).body(any(ParameterizedTypeReference.class));
    }

    @Test
    void getCredit_ShouldReturnCredit() throws Exception {
        ScoringDataDto scoringData = new ScoringDataDto();
        CreditDto expected = EntityFactory.createTestCreditDto();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TEST_CREDIT_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(scoringData)).thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CreditDto.class)).thenReturn(expected);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        CreditDto result = calculatorClientService.getCredit(scoringData);

        assertEquals(expected, result);
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).body(CreditDto.class);
    }

    @Test
    void getOffers_ShouldThrowOnRestClientException(){

        LoanStatementRequestDto request = EntityFactory.createLoanRequest();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TEST_OFFERS_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RestClientException("Error"));

        assertThrows(CalculatorServiceException.class, () ->
                calculatorClientService.getOffers(request));
    }

    @Test
    void getCredit_ShouldThrowOnNullResponse(){

        ScoringDataDto scoringData = new ScoringDataDto();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TEST_CREDIT_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(scoringData)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(CreditDto.class)).thenReturn(null);

        assertThrows(CreditRequestFailedException.class, () ->
                calculatorClientService.getCredit(scoringData));

        verify(responseSpec).onStatus(any(), any());
    }

    @Test
    void getOffers_ShouldThrowOnHttpError(){

        LoanStatementRequestDto request = EntityFactory.createLoanRequest();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TEST_OFFERS_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        doThrow(new CalculatorServiceException("Error"))
                .when(responseSpec).onStatus(any(), any());

        assertThrows(CalculatorServiceException.class, () ->
                calculatorClientService.getOffers(request));
    }
}