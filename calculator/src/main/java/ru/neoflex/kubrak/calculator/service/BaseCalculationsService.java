package ru.neoflex.kubrak.calculator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Slf4j
@Service
public class BaseCalculationsService {

    @Value("${credit.insurance.cost.percentage}")
    private BigDecimal insuranceCostPercentage;
    @Value("${credit.insurance.cost.minimum}")
    private BigDecimal insuranceCostMinimum;
    private static final int BIG_DECIMAL_SCALE_10 = 10;
    private static final int BIG_DECIMAL_SCALE_2 = 2;
    private static final BigDecimal ONE_HUNDRED_FOR_PERCENT_CALC = BigDecimal.valueOf(100);
    private static final BigDecimal MONTH_AMOUNT_IN_YEAR = BigDecimal.valueOf(12);

    public BigDecimal calculateInsuranceCost(BigDecimal amount) {

        log.info("Calculating insurance cost for amount: {}", amount);

        BigDecimal percentage = insuranceCostPercentage.divide(ONE_HUNDRED_FOR_PERCENT_CALC, BIG_DECIMAL_SCALE_10, RoundingMode.HALF_UP);
        log.debug("Insurance percentage (divided by 100): {}", percentage);

        BigDecimal calculatedCost = amount.multiply(percentage);
        log.debug("Calculated cost before minimum check: {}", calculatedCost);

        BigDecimal result = calculatedCost.max(insuranceCostMinimum)
                .setScale(BIG_DECIMAL_SCALE_2, RoundingMode.HALF_UP);
        log.info("Final insurance cost (after min {} check): {}", insuranceCostMinimum, result);

        return result;
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal rate, Integer term) {

        log.info("Calculating monthly payment for amount: {}, rate: {}, term: {}", amount, rate, term);
        log.debug("Used formula: (amount×monthlyRate×(1+monthlyRate)^term))/((1+monthlyRate)^term − 1)");

        MathContext mc = new MathContext(BIG_DECIMAL_SCALE_10, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = rate.divide(MONTH_AMOUNT_IN_YEAR, mc).divide(ONE_HUNDRED_FOR_PERCENT_CALC, mc);
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate, mc);
        BigDecimal powResult = onePlusRate.pow(term, mc);
        BigDecimal denominator = powResult.subtract(BigDecimal.ONE);
        BigDecimal numerator = amount.multiply(monthlyRate, mc).multiply(powResult);
        BigDecimal result = numerator.divide(denominator, BIG_DECIMAL_SCALE_2, RoundingMode.HALF_UP);

        log.info("Final monthly payment: {}", result);

        return result;
    }
}
