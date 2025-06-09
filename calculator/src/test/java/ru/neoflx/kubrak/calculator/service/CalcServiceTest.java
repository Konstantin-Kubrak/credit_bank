package ru.neoflx.kubrak.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.neoflx.kubrak.calculator.dto.LoanOfferDto;
import ru.neoflx.kubrak.calculator.dto.LoanStatementRequestDto;
import ru.neoflx.kubrak.calculator.util.PrepareTestDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульное тестирование - CalcService:")
class CalcServiceTest {

    @InjectMocks
    private CalcService calcService;
    LoanStatementRequestDto request;

    @BeforeEach
    void setUp() {

        // Устанавливаем значения из properties файла
        //TODO получать переменные из проперти файла
        ReflectionTestUtils.setField(calcService, "baseRate", BigDecimal.valueOf(15.0));
        ReflectionTestUtils.setField(calcService, "insuranceRateAdjustment", BigDecimal.valueOf(-3.0));
        ReflectionTestUtils.setField(calcService, "salaryClientRateAdjustment", BigDecimal.valueOf(-1.0));
        ReflectionTestUtils.setField(calcService, "insuranceCostPercentage", BigDecimal.valueOf(5.0));
        ReflectionTestUtils.setField(calcService, "insuranceCostMinimum", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(calcService, "minimumRate", BigDecimal.valueOf(5.0));

        request = PrepareTestDto.createValidLsrDto();
    }
    
    @Test
    void calculateLoanOffers_shouldReturnFourOffers() {

        List<LoanOfferDto> offers = calcService.calculateLoanOffers(request);

        assertEquals(4, offers.size());
        // Проверяем что предложения отсортированы от худшего к лучшему (по ставке)
        assertTrue(offers.get(0).getRate().compareTo(offers.get(1).getRate()) > 0);
        assertTrue(offers.get(1).getRate().compareTo(offers.get(2).getRate()) > 0);
        assertTrue(offers.get(2).getRate().compareTo(offers.get(3).getRate()) > 0);
    }

    @Test
    void createOffer_withoutInsuranceAndSalary_shouldReturnBaseRate() {

        LoanOfferDto offer = calcService.createOffer(request, false, false);

        assertNotNull(offer.getStatementId());
        assertEquals(request.getAmount(), offer.getRequestedAmount());
        assertEquals(request.getTerm(), offer.getTerm());
        assertFalse(offer.getIsInsuranceEnabled());
        assertFalse(offer.getIsSalaryClient());
        assertEquals(BigDecimal.valueOf(15.0), offer.getRate());
        assertEquals(request.getAmount(), offer.getTotalAmount());
        assertNotNull(offer.getMonthlyPayment());
    }

    @Test
    void createOffer_withInsurance_shouldApplyRateAdjustment() {

        LoanOfferDto offer = calcService.createOffer(request, true, false);

        assertEquals(BigDecimal.valueOf(12.0), offer.getRate()); // 15 - 3
        assertTrue(offer.getTotalAmount().compareTo(request.getAmount()) > 0); // с учетом страховки
    }

    @Test
    void createOffer_withSalaryClient_shouldApplyRateAdjustment() {

        LoanOfferDto offer = calcService.createOffer(request, false, true);

        assertEquals(BigDecimal.valueOf(14.0), offer.getRate()); // 15 - 1
    }

    @Test
    void createOffer_withInsuranceAndSalary_shouldApplyBothAdjustments() {

        LoanOfferDto offer = calcService.createOffer(request, true, true);

        assertEquals(BigDecimal.valueOf(11.0), offer.getRate()); // 15 - 3 - 1
    }

    @Test
    void createOffer_rateShouldNotGoBelowMinimum() {

        // Устанавливаем большую корректировку, которая опустит ставку ниже минимума
        ReflectionTestUtils.setField(calcService, "insuranceRateAdjustment", BigDecimal.valueOf(-20.0));

        
        LoanOfferDto offer = calcService.createOffer(request, true, false);

        assertEquals(BigDecimal.valueOf(5.0), offer.getRate()); // минимум
    }

    @Test
    void calculateInsuranceCost_shouldReturnCorrectValue() {

        // Для суммы больше минимальной
        BigDecimal amount1 = BigDecimal.valueOf(300000);
        BigDecimal expected1 = amount1.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected1, calcService.calculateInsuranceCost(amount1));

        // Для суммы меньше минимальной
        BigDecimal amount2 = BigDecimal.valueOf(100000);
        assertEquals(BigDecimal.valueOf(10000).setScale(2, RoundingMode.HALF_UP), calcService.calculateInsuranceCost(amount2));
    }

    @Test
    void calculateMonthlyPayment_shouldReturnCorrectValue() {

        BigDecimal amount = BigDecimal.valueOf(100000);
        BigDecimal rate = BigDecimal.valueOf(15.0);
        int term = 12;

        BigDecimal result = calcService.calculateMonthlyPayment(amount, rate, term);

        // Проверяем примерное значение
        assertTrue(result.compareTo(BigDecimal.valueOf(9000)) > 0);
        assertTrue(result.compareTo(BigDecimal.valueOf(9100)) < 0);
    }
    
}