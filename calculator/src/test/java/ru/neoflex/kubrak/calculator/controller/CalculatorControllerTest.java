package ru.neoflex.kubrak.calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.neoflex.kubrak.calculator.dto.CreditDto;
import ru.neoflex.kubrak.calculator.dto.LoanOfferDto;
import ru.neoflex.kubrak.calculator.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.calculator.dto.ScoringDataDto;
import ru.neoflex.kubrak.calculator.model.enums.EmploymentStatus;
import ru.neoflex.kubrak.calculator.model.enums.Position;
import ru.neoflex.kubrak.calculator.service.CalcService;
import ru.neoflex.kubrak.calculator.service.OffersService;
import ru.neoflex.kubrak.calculator.util.PrepareTestDto;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("Module testing - CalculatorController:")
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CalcService calcService;

    @Mock
    private OffersService offersService;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private CalculatorController calculatorController;

    private LoanStatementRequestDto loanStatementRequestDto;
    private ScoringDataDto scoringData;

    @BeforeEach
    void setUp(){

        loanStatementRequestDto = PrepareTestDto.createValidLsrDto();
        scoringData = PrepareTestDto.createValidScoringDataDto();
    }

    @Test
    @DisplayName("/calculator/offers - successful")
    void offers_shouldReturnValidResponse() throws Exception {

        List<LoanOfferDto> expectedOffers = List.of(PrepareTestDto.createLoanOffer(), PrepareTestDto.createLoanOffer());
        when(calcService.calculateLoanOffers(any(LoanStatementRequestDto.class))).thenReturn(expectedOffers);

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanStatementRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].requestedAmount").exists())
                .andExpect(jsonPath("$[0].rate").exists());
    }

    @Test
    @DisplayName("/calculator/offers - non-valid request data")
    void offers_shouldReturnBadRequestForInvalidData() throws Exception {

        LoanStatementRequestDto invalidRequest = new LoanStatementRequestDto();

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("/calculator/calc - successful")
    void calc_shouldReturnValidCreditDto() throws Exception {

        CreditDto expectedCredit = PrepareTestDto.createCreditDto();

        when(offersService.calculateCredit(any(ScoringDataDto.class)))
                .thenReturn(expectedCredit);

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoringData)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(expectedCredit.getAmount()))
                .andExpect(jsonPath("$.rate").value(expectedCredit.getRate()));
    }

    @Test
    @DisplayName("/calculator/calc - non-valid request data")
    void calc_shouldReturnBadRequestForInvalidData() throws Exception {

        ScoringDataDto invalidData = new ScoringDataDto();

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("/calculator/calc - throws exception for invalid employment data")
    void calc_shouldThrowForInvalidEmploymentData() throws Exception {

        ScoringDataDto invalidData = PrepareTestDto.createValidScoringDataDto();
        invalidData.getEmployment()
                .setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER)
                .setPosition(Position.MANAGER);

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("/calculator/calc - throws exception for unemployed")
    void calc_shouldThrowForUnemployed() throws Exception {
        ScoringDataDto invalidData = PrepareTestDto.createValidScoringDataDto();
        invalidData.getEmployment()
                .setEmploymentStatus(EmploymentStatus.UNEMPLOYED)
                .setPosition(Position.UNEMPLOYED);

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("/calculator/calc - throws exception when amount exceeds 24 salaries")
    void calc_shouldThrowWhenAmountExceeds24Salaries() throws Exception {
        ScoringDataDto invalidData = PrepareTestDto.createValidScoringDataDto();
        invalidData.setAmount(BigDecimal.valueOf(1000000));
        invalidData.getEmployment().setSalary(BigDecimal.valueOf(40000));

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidData)))
                .andExpect(status().isBadRequest());
    }

}