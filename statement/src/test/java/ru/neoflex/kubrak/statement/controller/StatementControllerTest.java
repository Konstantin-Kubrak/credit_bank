package ru.neoflex.kubrak.statement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.neoflex.kubrak.statement.dto.LoanOfferDto;
import ru.neoflex.kubrak.statement.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.statement.exception.DealServiceException;
import ru.neoflex.kubrak.statement.exception.PreScoringException;
import ru.neoflex.kubrak.statement.exception.ValidationException;
import ru.neoflex.kubrak.statement.service.StatementService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatementController.class)
class StatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatementService statementService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void statement_ValidRequest_ReturnsOk() throws Exception {
        LoanStatementRequestDto request = LoanStatementRequestDto.builder().build();
        List<LoanOfferDto> offers = List.of(new LoanOfferDto());

        when(statementService.getLoanOffers(request)).thenReturn(offers);

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void selectOffer_ValidOffer_ReturnsNoContent() throws Exception {
        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoanOfferDto())))
                .andExpect(status().isNoContent());
    }


    @Test
    void statement_PreScoringException_ReturnsBadRequest() throws Exception {
        LoanStatementRequestDto request = LoanStatementRequestDto.builder().build();

        when(statementService.getLoanOffers(any()))
                .thenThrow(new PreScoringException("Invalid email format"));

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format"));
    }

    @Test
    void statement_ValidationException_ReturnsBadRequest() throws Exception {
        LoanStatementRequestDto request = LoanStatementRequestDto.builder().build();

        when(statementService.getLoanOffers(any()))
                .thenThrow(new ValidationException("Required field is missing"));

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Required field is missing"));
    }

    @Test
    void statement_DealServiceException_ReturnsInternalServerError() throws Exception {
        LoanStatementRequestDto request = LoanStatementRequestDto.builder().build();

        when(statementService.getLoanOffers(any()))
                .thenThrow(new DealServiceException("Deal service unavailable"));

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Deal service unavailable"));
    }

    @Test
    void selectOffer_InvalidOffer_ReturnsBadRequest() throws Exception {
        doThrow(new ValidationException("Invalid offer"))
                .when(statementService).selectOffer(any());

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoanOfferDto())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid offer"));
    }

    @Test
    void selectOffer_DealServiceException_ReturnsInternalServerError() throws Exception {
        doThrow(new DealServiceException("Deal service error"))
                .when(statementService).selectOffer(any());

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoanOfferDto())))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Deal service error"));
    }
}