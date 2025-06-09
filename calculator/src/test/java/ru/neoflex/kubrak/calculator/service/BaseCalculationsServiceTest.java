package ru.neoflex.kubrak.calculator.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("Module testing - BaseCalculationsService:")
class BaseCalculationsServiceTest {

    @Autowired
    private BaseCalculationsService baseCalc;

    @Test
    void calculateInsuranceCost_shouldReturnCorrectValue() {

        BigDecimal amount1 = BigDecimal.valueOf(300000);
        BigDecimal expected1 = amount1.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected1, baseCalc.calculateInsuranceCost(amount1));

        BigDecimal amount2 = BigDecimal.valueOf(100000);
        assertEquals(BigDecimal.valueOf(10000).setScale(2, RoundingMode.HALF_UP), baseCalc.calculateInsuranceCost(amount2));
    }

    @Test
    void calculateMonthlyPayment_shouldReturnCorrectValue() {

        BigDecimal amount = BigDecimal.valueOf(100000);
        BigDecimal rate = BigDecimal.valueOf(15.0);
        int term = 12;

        BigDecimal result = baseCalc.calculateMonthlyPayment(amount, rate, term);

        assertTrue(result.compareTo(BigDecimal.valueOf(9000)) > 0);
        assertTrue(result.compareTo(BigDecimal.valueOf(9100)) < 0);
    }
}