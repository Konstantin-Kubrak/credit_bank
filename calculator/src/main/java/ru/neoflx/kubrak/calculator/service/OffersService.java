package ru.neoflx.kubrak.calculator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflx.kubrak.calculator.dto.CreditDto;
import ru.neoflx.kubrak.calculator.dto.EmploymentDto;
import ru.neoflx.kubrak.calculator.dto.PaymentScheduleElementDto;
import ru.neoflx.kubrak.calculator.dto.ScoringDataDto;
import ru.neoflx.kubrak.calculator.model.enums.Gender;
import ru.neoflx.kubrak.calculator.model.enums.MaritalStatus;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class OffersService {

    @Value("${credit.base-rate}")
    private BigDecimal baseRate;
    @Value("${credit.rate.adjustment.insurance}")
    private BigDecimal insuranceRateAdjustment;
    @Value("${credit.rate.adjustment.salary-client}")
    private BigDecimal salaryClientRateAdjustment;
    @Value("${credit.rate.adjustment.female}")
    private BigDecimal femaleRateAdjustment;
    @Value("${credit.rate.adjustment.married}")
    private BigDecimal marriedRateAdjustment;
    @Value("${credit.rate.adjustment.unemployed}")
    private BigDecimal unemployedRateAdjustment;
    @Value("${credit.rate.adjustment.self-employed}")
    private BigDecimal selfEmployedRateAdjustment;
    @Value("${credit.rate.adjustment.business-owner}")
    private BigDecimal businessOwnerRateAdjustment;
    @Value("${credit.rate.adjustment.top-manager}")
    private BigDecimal topManagerRateAdjustment;
    @Value("${credit.rate.adjustment.owner}")
    private BigDecimal ownerRateAdjustment;
    @Value("${credit.rate.adjustment.low-experience}")
    private BigDecimal lowExperienceRateAdjustment;
    @Value("${credit.rate.minimum}")
    private BigDecimal minimumRate;
    @Value("${credit.insurance.cost.percentage}")
    private BigDecimal insuranceCostPercentage;
    @Value("${credit.insurance.cost.minimum}")
    private BigDecimal insuranceCostMinimum;


    public CreditDto calculateCredit(ScoringDataDto scoringData) {

        log.debug("Starting credit calculation for scoring data: {}", scoringData);

        CreditDto credit = new CreditDto();
        credit.setAmount(scoringData.getAmount());
        credit.setTerm(scoringData.getTerm());
        credit.setIsInsuranceEnabled(scoringData.getIsInsuranceEnabled());
        credit.setIsSalaryClient(scoringData.getIsSalaryClient());

        // Calculate rate with all adjustments
        BigDecimal rate = calculateFinalRate(scoringData);
        log.debug("Called calculate final rate method, result: {}", rate);
        credit.setRate(rate);

        // Calculate total amount with insurance if enabled
        BigDecimal totalAmount = scoringData.getAmount();
        log.debug("Initial total amount: {}", totalAmount);
        if (scoringData.getIsInsuranceEnabled()) {
            BigDecimal insuranceCost = calculateInsuranceCost(scoringData.getAmount());
            log.debug("Called calculate insurance cost method, result: {}", insuranceCost);
            totalAmount = totalAmount.add(insuranceCost);
            log.debug("Total amount with insurance: {}", totalAmount);
        }

        // Calculate monthly payment and PSK
        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, rate, scoringData.getTerm());
        log.debug("Called calculate monthly payment method, result: {}", monthlyPayment);
        BigDecimal psk = calculatePSK(totalAmount, monthlyPayment, scoringData.getTerm());
        log.debug("Called calculate PSK method, result: {}%", psk);

        credit.setMonthlyPayment(monthlyPayment);
        credit.setPsk(psk);

        // Generate payment schedule
        List<PaymentScheduleElementDto> schedule = generatePaymentSchedule(totalAmount, rate, scoringData.getTerm());
        log.debug("Generated payment schedule with {} elements", schedule.size());
        credit.setPaymentSchedule(schedule);
        log.debug("Final credit details: {}", credit);

        return credit;
    }

    public BigDecimal calculateFinalRate(ScoringDataDto scoringData) {

        log.debug("Calculating final rate for scoring data: {}", scoringData);

        BigDecimal rate = baseRate;
        log.debug("Initial base rate: {}", rate);

        // Apply basic adjustments
        if (scoringData.getIsInsuranceEnabled()) {
            rate = rate.add(insuranceRateAdjustment);
            log.debug("After insurance adjustment (+{}): {}", insuranceRateAdjustment, rate);
        }
        if (scoringData.getIsSalaryClient()) {
            rate = rate.add(salaryClientRateAdjustment);
            log.debug("After salary client adjustment (+{}): {}", salaryClientRateAdjustment, rate);
        }
        if (scoringData.getGender() == Gender.FEMALE) {
            rate = rate.add(femaleRateAdjustment);
            log.debug("After female adjustment (+{}): {}", femaleRateAdjustment, rate);
        }
        if (scoringData.getMaritalStatus() == MaritalStatus.MARRIED) {
            rate = rate.add(marriedRateAdjustment);
            log.debug("After married adjustment (+{}): {}", marriedRateAdjustment, rate);
        }

        // Apply employment adjustments
        EmploymentDto employment = scoringData.getEmployment();
        rate = switch (employment.getEmploymentStatus()) {
            case UNEMPLOYED -> rate.add(unemployedRateAdjustment);
            case SELF_EMPLOYED -> rate.add(selfEmployedRateAdjustment);
            case BUSINESS_OWNER -> rate.add(businessOwnerRateAdjustment);
            default -> rate;
        };
        log.debug("After employment status adjustment (+{}): {}", employment.getEmploymentStatus(), rate);

        // Apply position adjustments
        rate = switch (employment.getPosition()) {
            case TOP_MANAGER -> rate.add(topManagerRateAdjustment);
            case OWNER -> rate.add(ownerRateAdjustment);
            default -> rate;
        };
        log.debug("After employment position adjustment (+{}): {}", employment.getPosition(), rate);

        // Apply experience adjustment
        if (employment.getWorkExperienceTotal() < 12) {
            rate = rate.add(lowExperienceRateAdjustment);
            log.debug("After low experience adjustment (+{}): {}", lowExperienceRateAdjustment, rate);
        }

        // Ensure rate is not below minimum
        rate = rate.max(minimumRate);
        log.debug("Final rate after minimum rate check (min {}): {}", minimumRate, rate);

        return rate;
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

    public BigDecimal calculatePSK(BigDecimal amount, BigDecimal monthlyPayment, Integer term) {

        log.debug("Calculating PSK for amount: {}, monthlyPayment: {}, term: {}", amount, monthlyPayment, term);

        BigDecimal totalPayments = monthlyPayment.multiply(BigDecimal.valueOf(term));
        log.debug("Total payments over {} months: {}", term, totalPayments);

        BigDecimal psk = totalPayments.subtract(amount)
                .divide(amount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        log.debug("Calculated PSK: {}%", psk);

        return psk;
    }

    public List<PaymentScheduleElementDto> generatePaymentSchedule(BigDecimal amount, BigDecimal rate, Integer term) {

        log.debug("Generating payment schedule for amount: {}, rate: {}, term: {}", amount, rate, term);

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        log.debug("Monthly rate: {}", monthlyRate);
        BigDecimal remainingDebt = amount;
        log.debug("Initial remaining debt: {}", remainingDebt);
        LocalDate paymentDate = LocalDate.now().plusMonths(1);
        log.debug("First payment date: {}", paymentDate);

        for (int i = 1; i <= term; i++) {
            log.debug("Generating payment element {} of {}", i, term);

            PaymentScheduleElementDto element = new PaymentScheduleElementDto();
            element.setNumber(i);
            element.setDate(paymentDate);

            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            log.debug("Interest payment for period {}: {}", i, interestPayment);

            BigDecimal monthlyPayment = calculateMonthlyPayment(amount, rate, term);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);
            log.debug("Initial debt payment for period {}: {}", i, debtPayment);

            // For last payment, adjust to match remaining debt
            if (i == term) {
                debtPayment = remainingDebt;
                monthlyPayment = debtPayment.add(interestPayment);
                log.debug("Final payment adjustment - debtPayment: {}, monthlyPayment: {}", debtPayment, monthlyPayment);
            }

            element.setTotalPayment(monthlyPayment);
            element.setInterestPayment(interestPayment);
            element.setDebtPayment(debtPayment);

            remainingDebt = remainingDebt.subtract(debtPayment);
            if (remainingDebt.compareTo(BigDecimal.ZERO) < 0) {
                remainingDebt = BigDecimal.ZERO;
            }
            element.setRemainingDebt(remainingDebt);
            log.debug("Remaining debt after payment {}: {}", i, remainingDebt);

            schedule.add(element);
            paymentDate = paymentDate.plusMonths(1);
        }

        log.debug("Completed payment schedule generation");

        return schedule;
    }
}
