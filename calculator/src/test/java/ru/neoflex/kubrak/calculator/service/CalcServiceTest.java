package ru.neoflex.kubrak.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.neoflex.kubrak.calculator.dto.LoanOfferDto;
import ru.neoflex.kubrak.calculator.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.calculator.util.PrepareTestDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
@DisplayName("Module testing - CalcService:")
class CalcServiceTest {

    @MockitoBean
    private BaseCalculationsService baseCalc;
    @Autowired
    private CalcService calcService;
    private LoanStatementRequestDto request;

    @BeforeEach
    void setUp() {

        when(baseCalc.calculateInsuranceCost(any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(5000).setScale(2, RoundingMode.HALF_UP));
        when(baseCalc.calculateMonthlyPayment(any(BigDecimal.class), any(BigDecimal.class), any(Integer.class)))
                .thenReturn(BigDecimal.valueOf(9000).setScale(2, RoundingMode.HALF_UP));
        request = PrepareTestDto.createValidLsrDto();
    }
    
    @Test
    void calculateLoanOffers_shouldReturnFourOffers() {

        List<LoanOfferDto> offers = calcService.calculateLoanOffers(request);

        assertEquals(4, offers.size());
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

        ReflectionTestUtils.setField(calcService, "insuranceRateAdjustment", BigDecimal.valueOf(-20.0));
        LoanOfferDto offer = calcService.createOffer(request, true, false);

        assertEquals(BigDecimal.valueOf(5.0), offer.getRate()); // минимум
    }

}