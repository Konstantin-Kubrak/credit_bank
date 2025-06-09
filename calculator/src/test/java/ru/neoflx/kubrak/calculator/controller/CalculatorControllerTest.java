package ru.neoflx.kubrak.calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.neoflx.kubrak.calculator.dto.CreditDto;
import ru.neoflx.kubrak.calculator.dto.LoanStatementRequestDto;
import ru.neoflx.kubrak.calculator.dto.ScoringDataDto;
import ru.neoflx.kubrak.calculator.util.PrepareTestDto;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("Интеграционное тестирование - CalculatorController:")
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

/*    @Mock
    private CalcService calcService;

    @Mock
    private OffersService offersService;*/

    @Autowired
    private ObjectMapper objectMapper;

/*    @InjectMocks
    private CalculatorController calculatorController;*/

    private LoanStatementRequestDto loanStatementRequestDto;
    private ScoringDataDto scoringData;

    @BeforeEach
    void init(){

        loanStatementRequestDto = PrepareTestDto.createValidLsrDto();
        scoringData = PrepareTestDto.createValidScoringDataDto();
    }

    @Test
    void offers_shouldReturnValidResponse() throws Exception {

        // Подготовка тестовых данных
        //List<LoanOfferDto> expectedOffers = List.of(createLoanOffer(), createLoanOffer());
        // Мокируем сервис
        //when(calcService.calculateLoanOffers(any(LoanStatementRequestDto.class))).thenReturn(expectedOffers);

        // Выполняем запрос и проверяем результат
        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanStatementRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].requestedAmount").exists())
                .andExpect(jsonPath("$[0].rate").exists());
    }

    @Test
    void offers_shouldReturnBadRequestForInvalidData() throws Exception {
        // Невалидный запрос (без обязательных полей)
        LoanStatementRequestDto invalidRequest = new LoanStatementRequestDto();

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calc_shouldReturnValidCreditDto() throws Exception {
        // Подготовка тестовых данных
        CreditDto expectedCredit = createCreditDto();

        // Мокируем сервис
/*        when(offersService.calculateCredit(any(ScoringDataDto.class)))
                .thenReturn(expectedCredit);*/

        // Выполняем запрос и проверяем результат
        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoringData)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(expectedCredit.getAmount()))
                .andExpect(jsonPath("$.rate").value(expectedCredit.getRate()));
    }

    @Test
    void calc_shouldReturnBadRequestForInvalidData() throws Exception {
        // Невалидные данные (без обязательных полей)
        ScoringDataDto invalidData = new ScoringDataDto();

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidData)))
                .andExpect(status().isBadRequest());
    }


/*    private LoanOfferDto createLoanOffer() {
        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(UUID.randomUUID());
        offer.setRequestedAmount(BigDecimal.valueOf(100000));
        offer.setTotalAmount(BigDecimal.valueOf(105000));
        offer.setTerm(12);
        offer.setMonthlyPayment(BigDecimal.valueOf(9000));
        offer.setRate(BigDecimal.valueOf(12.5));
        offer.setIsInsuranceEnabled(false);
        offer.setIsSalaryClient(true);
        return offer;
    }*/


    private CreditDto createCreditDto() {
        CreditDto credit = new CreditDto();
        credit.setAmount(BigDecimal.valueOf(100000));
        credit.setTerm(12);
        credit.setRate(BigDecimal.valueOf(15.0));
        credit.setMonthlyPayment(BigDecimal.valueOf(9000));
        credit.setIsInsuranceEnabled(false);
        credit.setIsSalaryClient(false);
        return credit;
    }
}