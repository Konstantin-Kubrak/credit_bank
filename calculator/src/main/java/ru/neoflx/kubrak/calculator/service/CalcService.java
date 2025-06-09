package ru.neoflx.kubrak.calculator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflx.kubrak.calculator.dto.LoanOfferDto;
import ru.neoflx.kubrak.calculator.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CalcService {

    @Value("${credit.base-rate}")
    private BigDecimal baseRate;
    @Value("${credit.rate.adjustment.insurance}")
    private BigDecimal insuranceRateAdjustment;
    @Value("${credit.rate.adjustment.salary-client}")
    private BigDecimal salaryClientRateAdjustment;
    @Value("${credit.insurance.cost.percentage}")
    private BigDecimal insuranceCostPercentage;
    @Value("${credit.insurance.cost.minimum}")
    private BigDecimal insuranceCostMinimum;
    @Value("${credit.rate.minimum}")
    private BigDecimal minimumRate;

    public List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request) {

        log.debug("Starting loan offers calculation for request: {}", request);
        List<LoanOfferDto> offers = new ArrayList<>();

        // Generate all combinations of insurance and salary client options
        offers.add(createOffer(request, false, false));
        offers.add(createOffer(request, false, true));
        offers.add(createOffer(request, true, false));
        offers.add(createOffer(request, true, true));

        // Sort offers from worst to best (by rate)
        offers.sort((o1, o2) -> o2.getRate().compareTo(o1.getRate()));
        log.debug("Generated {} offers, sorted from worst to best: {}", offers.size(), offers);

        return offers;
    }

    public LoanOfferDto createOffer(LoanStatementRequestDto request, Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        log.debug("Creating offer for request: {}, isInsuranceEnabled: {}, isSalaryClient: {}",
                request, isInsuranceEnabled, isSalaryClient);
        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(UUID.randomUUID());
        offer.setRequestedAmount(request.getAmount());
        offer.setTerm(request.getTerm());
        offer.setIsInsuranceEnabled(isInsuranceEnabled);
        offer.setIsSalaryClient(isSalaryClient);

        // Calculate base rate
        BigDecimal rate = baseRate;
        log.debug("Initial base rate: {}", rate);

        // Apply adjustments
        if (isInsuranceEnabled) {
            rate = rate.add(insuranceRateAdjustment);
            log.debug("After insurance adjustment (+{}): {}", insuranceRateAdjustment, rate);
        }
        if (isSalaryClient) {
            rate = rate.add(salaryClientRateAdjustment);
            log.debug("After salary client adjustment (+{}): {}", salaryClientRateAdjustment, rate);
        }

        // Calculate total amount with insurance if enabled
        BigDecimal totalAmount = request.getAmount();
        log.debug("Initial total amount: {}", totalAmount);

        if (isInsuranceEnabled) {
            BigDecimal insuranceCost = calculateInsuranceCost(totalAmount);
            log.debug("Calculated insurance cost: {}", insuranceCost);
            totalAmount = totalAmount.add(insuranceCost);
            log.debug("Total amount with insurance: {}", totalAmount);
        }

        // Ensure rate doesn't go below minimum
        rate = rate.max(minimumRate);
        log.debug("Final rate after minimum rate check (min {}): {}", minimumRate, rate);

        // Calculate monthly payment
        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, rate, request.getTerm());
        log.debug("Calculated monthly payment: {}", monthlyPayment);

        offer.setTotalAmount(totalAmount);
        offer.setRate(rate);
        offer.setMonthlyPayment(monthlyPayment);
        log.debug("Created offer: {}", offer);

        return offer;
    }

    //TODO вынести в отдельный класс дублирующиеся методы
    public BigDecimal calculateInsuranceCost(BigDecimal amount) {

        log.debug("Calculating insurance cost for amount: {}", amount);

        BigDecimal percentage = insuranceCostPercentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        log.debug("Insurance percentage (divided by 100): {}", percentage);

        BigDecimal calculatedCost = amount.multiply(percentage);
        log.debug("Calculated cost before minimum check: {}", calculatedCost);

        BigDecimal result = calculatedCost.max(insuranceCostMinimum)
                .setScale(2, RoundingMode.HALF_UP);
        log.debug("Final insurance cost (after min {} check): {}", insuranceCostMinimum, result);

        return result;
    }

    //TODO вынести в отдельный класс дублирующиеся методы
    public BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal rate, Integer term) {

        //P= (S×r×(1+r)^n))/((1+r)^n) − 1)
        //P — ежемесячный платеж,
        //S — сумма кредита (amount),
        //r — месячная процентная ставка (monthlyRate),
        //n — количество месяцев (term)
        log.debug("Calculating monthly payment for amount: {}, rate: {}, term: {}", amount, rate, term);
        log.debug("Used formula: (amount×monthlyRate×(1+monthlyRate)^term))/((1+monthlyRate)^term − 1)");

        // Контекст для вычислений с округлением (10 знаков, HALF_UP)
        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        // 1. Рассчитываем месячную ставку r (годовая ставка / 12 / 100)
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(1200), mc);
        // 2. Вычисляем (1 + r)
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate, mc);
        // 3. Возводим (1 + r) в степень n (срок в месяцах)
        BigDecimal powResult = onePlusRate.pow(term, mc);
        // 4. Вычисляем знаменатель: ((1 + r)^n) - 1
        BigDecimal denominator = powResult.subtract(BigDecimal.ONE);
        // 5. Вычисляем числитель: S * r * ((1 + r)^n)
        BigDecimal numerator = amount.multiply(monthlyRate, mc).multiply(powResult);
        // 6. Делим числитель на знаменатель и округляем до 2 знаков
        BigDecimal result = numerator.divide(denominator, 2, RoundingMode.HALF_UP);

        log.debug("Final monthly payment: {}", result);
        return result;
    }
}
