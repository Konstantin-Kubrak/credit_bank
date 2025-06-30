package ru.neoflex.kubrak.deal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.service.DealService;
import ru.neoflex.kubrak.deal.service.StatementService;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StatementControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StatementService statementService;

    @Mock
    private DealService dealService;

    @InjectMocks
    private StatementController statementController;

    private final String statementUri = "/deal/statement";
    private final String offerSelectUri = "/deal/offer/select";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statementController).build();
    }

    @Test
    void statement_ShouldReturnLoanOffers() throws Exception {

        LoanStatementRequestDto requestDto = EntityFactory.createTestLoanRequest();
        List<LoanOfferDto> expectedOffers = EntityFactory.createExpectedOffers();

        when(dealService.getLoanOfferList(requestDto)).thenReturn(expectedOffers);

        mockMvc.perform(post(statementUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].statementId").exists())
                .andExpect(jsonPath("$[0].requestedAmount").value(100000))
                .andExpect(jsonPath("$[1].rate").value(8));

        verify(dealService).getLoanOfferList(requestDto);
    }

    @Test
    void statement_ShouldReturnServiceUnavailableWhenCalculatorFails() throws Exception {

        LoanStatementRequestDto requestDto = EntityFactory.createTestLoanRequest();
        String errorMessage = "Calculator service unavailable";

        when(dealService.getLoanOfferList(requestDto))
                .thenThrow(new CalculatorServiceException(errorMessage));

        mockMvc.perform(post(statementUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Calculator service error: " + errorMessage));

        verify(dealService).getLoanOfferList(requestDto);
    }

    @Test
    void statement_ShouldValidateRequest() throws Exception {

        LoanStatementRequestDto invalidDto = new LoanStatementRequestDto();

        mockMvc.perform(post(statementUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dealService);
    }

    @Test
    void selectOffer_ShouldReturnOkWhenSuccess() throws Exception {

        LoanOfferDto offerDto = EntityFactory.createExpectedOffers().getFirst()
                .setStatementId(UUID.randomUUID());

        doNothing().when(statementService).setStatementLoanOffer(offerDto);

        mockMvc.perform(post(offerSelectUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerDto)))
                .andExpect(status().isOk());

        verify(statementService).setStatementLoanOffer(offerDto);
    }

    @Test
    void selectOffer_ShouldReturnNotFoundWhenStatementNotFound() throws Exception {

        LoanOfferDto offerDto = EntityFactory.createExpectedOffers().getFirst()
                .setStatementId(UUID.randomUUID());
        String errorMessage = "Statement with ID " + offerDto.getStatementId() + " not found";

        doThrow(new StatementNotFoundException(offerDto.getStatementId()))
                .when(statementService).setStatementLoanOffer(offerDto);

        mockMvc.perform(post(offerSelectUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(statementService).setStatementLoanOffer(offerDto);

    }

    @Test
    void selectOffer_ShouldValidateRequest() throws Exception {

        LoanOfferDto invalidDto = new LoanOfferDto();

        mockMvc.perform(post(offerSelectUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(statementService);
    }
}