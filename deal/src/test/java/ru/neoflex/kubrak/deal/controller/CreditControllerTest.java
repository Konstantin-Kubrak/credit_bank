package ru.neoflex.kubrak.deal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.neoflex.kubrak.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.kubrak.deal.exception.CreditRequestFailedException;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.service.CreditService;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CreditController.class)
class CreditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreditService creditService;

    UUID statementId;
    FinishRegistrationRequestDto frrDto;

    @BeforeEach
    void setUp() {

        statementId = UUID.randomUUID();
        frrDto = EntityFactory.createTestFinishRegistrationRequestDto();
    }

    @Test
    void calculate_ShouldReturnCreatedWhenSuccess() throws Exception {

        doNothing().when(creditService).finishCreditRegistration(statementId, frrDto);

        mockMvc.perform(post("/deal/calculate/{statementId}", statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(frrDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(creditService).finishCreditRegistration(statementId, frrDto);
    }

    @Test
    void calculate_ShouldReturnNotFoundWhenStatementNotFound() throws Exception {

        String errorMessage = "Statement with ID " + statementId + " not found";
        doThrow(new StatementNotFoundException(statementId))
                .when(creditService).finishCreditRegistration(statementId, frrDto);

        mockMvc.perform(post("/deal/calculate/{statementId}", statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(frrDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(creditService).finishCreditRegistration(statementId, frrDto);
    }

    @Test
    void calculate_ShouldReturnInternalServerErrorWhenCreditCalculationFails() throws Exception {

        String errorMessage = "Credit calculation failed";
        doThrow(new CreditRequestFailedException(errorMessage))
                .when(creditService).finishCreditRegistration(statementId, frrDto);

        mockMvc.perform(post("/deal/calculate/{statementId}", statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(frrDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(errorMessage));

        verify(creditService).finishCreditRegistration(statementId, frrDto);
    }

    @Test
    void calculate_ShouldValidateRequest() throws Exception {
        FinishRegistrationRequestDto invalidDto = new FinishRegistrationRequestDto();

        mockMvc.perform(post("/deal/calculate/{statementId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(creditService, never()).finishCreditRegistration(any(), any());
    }

    @Test
    void calculate_ShouldValidatePathVariable() throws Exception {

        mockMvc.perform(post("/deal/calculate/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(frrDto)))
                .andExpect(status().isBadRequest());

        verify(creditService, never()).finishCreditRegistration(any(), any());
    }
}