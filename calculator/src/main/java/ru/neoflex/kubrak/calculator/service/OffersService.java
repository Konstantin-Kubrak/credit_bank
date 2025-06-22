package ru.neoflex.kubrak.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.calculator.dto.CreditDto;
import ru.neoflex.kubrak.calculator.dto.EmploymentDto;
import ru.neoflex.kubrak.calculator.dto.PaymentScheduleElementDto;
import ru.neoflex.kubrak.calculator.dto.ScoringDataDto;
import ru.neoflex.kubrak.calculator.exception.InvalidCreditAmountException;
import ru.neoflex.kubrak.calculator.exception.InvalidEmploymentStatusOrPositionException;
import ru.neoflex.kubrak.calculator.model.enums.EmploymentPosition;
import ru.neoflex.kubrak.calculator.model.enums.EmploymentStatus;
import ru.neoflex.kubrak.calculator.model.enums.Gender;
import ru.neoflex.kubrak.calculator.model.enums.MaritalStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
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
    @Value("${credit.minimum.work-experience-month}")
    private Integer LOW_EXPERIENCE_ENDS_AT;

    private static final int BIG_DECIMAL_SCALE_2 = 2;
    private static final int BIG_DECIMAL_SCALE_4 = 4;
    private static final int BIG_DECIMAL_SCALE_10 = 10;
    private static final BigDecimal ONE_HUNDRED_FOR_PERCENT_CALC = BigDecimal.valueOf(100);
    private static final BigDecimal MONTH_AMOUNT_IN_YEAR = BigDecimal.valueOf(12);
    private static final BigDecimal MONTH_AMOUNT_IN_2_YEARS = BigDecimal.valueOf(24);

    private final BaseCalculationsService baseCalc;

    public CreditDto calculateCredit(ScoringDataDto scoringData) throws
            InvalidEmploymentStatusOrPositionException, InvalidCreditAmountException {

        log.info("Starting credit calculation for scoring data: {}", scoringData);

        scoringDataValidation(scoringData);

        CreditDto credit = new CreditDto()
                .setAmount(scoringData.getAmount())
                .setTerm(scoringData.getTerm())
                .setIsInsuranceEnabled(scoringData.getIsInsuranceEnabled())
                .setIsSalaryClient(scoringData.getIsSalaryClient());

        BigDecimal rate = calculateFinalRate(scoringData);
        log.debug("Called calculate final rate method, result: {}", rate);
        credit.setRate(rate);

        BigDecimal totalAmount = scoringData.getAmount();
        log.debug("Initial total amount: {}", totalAmount);
        if (scoringData.getIsInsuranceEnabled()) {
            BigDecimal insuranceCost = baseCalc.calculateInsuranceCost(scoringData.getAmount());
            log.debug("Called calculate insurance cost method, result: {}", insuranceCost);
            totalAmount = totalAmount.add(insuranceCost);
            log.debug("Total amount with insurance: {}", totalAmount);
        }

        BigDecimal monthlyPayment = baseCalc.calculateMonthlyPayment(totalAmount, rate, scoringData.getTerm());
        log.debug("Called calculate monthly payment method, result: {}", monthlyPayment);
        BigDecimal psk = calculatePSK(totalAmount, monthlyPayment, scoringData.getTerm());
        log.debug("Called calculate PSK method, result: {}%", psk);

        credit.setMonthlyPayment(monthlyPayment).setPsk(psk);

        List<PaymentScheduleElementDto> schedule = generatePaymentSchedule(totalAmount, rate, scoringData.getTerm());
        log.debug("Generated payment schedule with {} elements", schedule.size());
        credit.setPaymentSchedule(schedule);
        log.info("Final credit details: {}", credit);

        return credit;
    }

    public BigDecimal calculateFinalRate(ScoringDataDto scoringData) {

        log.info("Calculating final rate for scoring data: {}", scoringData);

        BigDecimal rate = baseRate;
        log.debug("Initial base rate: {}", rate);

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

        EmploymentDto employment = scoringData.getEmployment();
        rate = switch (employment.getEmploymentStatus()) {
            case UNEMPLOYED -> rate.add(unemployedRateAdjustment);
            case SELF_EMPLOYED -> rate.add(selfEmployedRateAdjustment);
            case BUSINESS_OWNER -> rate.add(businessOwnerRateAdjustment);
            default -> rate;
        };
        log.debug("After employment status adjustment (+{}): {}", employment.getEmploymentStatus(), rate);

        rate = switch (employment.getEmploymentPosition()) {
            case TOP_MANAGER -> rate.add(topManagerRateAdjustment);
            case OWNER -> rate.add(ownerRateAdjustment);
            default -> rate;
        };
        log.debug("After employment position adjustment (+{}): {}", employment.getEmploymentPosition(), rate);

        if (employment.getWorkExperienceTotal() < LOW_EXPERIENCE_ENDS_AT) {
            rate = rate.add(lowExperienceRateAdjustment);
            log.debug("After low experience adjustment (+{}): {}", lowExperienceRateAdjustment, rate);
        }

        rate = rate.max(minimumRate);
        log.info("Final rate after minimum rate check (min {}): {}", minimumRate, rate);

        return rate;
    }

    public BigDecimal calculatePSK(BigDecimal amount, BigDecimal monthlyPayment, Integer term) {

        log.info("Calculating PSK for amount: {}, monthlyPayment: {}, term: {}", amount, monthlyPayment, term);

        BigDecimal totalPayments = monthlyPayment.multiply(BigDecimal.valueOf(term));
        log.debug("Total payments over {} months: {}", term, totalPayments);

        BigDecimal psk = totalPayments.subtract(amount)
                .divide(amount, BIG_DECIMAL_SCALE_4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED_FOR_PERCENT_CALC)
                .setScale(BIG_DECIMAL_SCALE_2, RoundingMode.HALF_UP);
        log.info("Calculated PSK: {}%", psk);

        return psk;
    }

    public List<PaymentScheduleElementDto> generatePaymentSchedule(BigDecimal amount, BigDecimal rate, Integer term) {

        log.info("Generating payment schedule for amount: {}, rate: {}, term: {}", amount, rate, term);

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        BigDecimal monthlyRate = rate
                .divide(MONTH_AMOUNT_IN_YEAR, BIG_DECIMAL_SCALE_10, RoundingMode.HALF_UP)
                .divide(ONE_HUNDRED_FOR_PERCENT_CALC, BIG_DECIMAL_SCALE_10, RoundingMode.HALF_UP);
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

            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate).setScale(BIG_DECIMAL_SCALE_2, RoundingMode.HALF_UP);
            log.debug("Interest payment for period {}: {}", i, interestPayment);

            BigDecimal monthlyPayment = baseCalc.calculateMonthlyPayment(amount, rate, term);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);
            log.debug("Initial debt payment for period {}: {}", i, debtPayment);

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

        log.info("Completed payment schedule generation");

        return schedule;
    }

    public void scoringDataValidation(ScoringDataDto scoringDataDto) throws
            InvalidEmploymentStatusOrPositionException,
            InvalidCreditAmountException {

        log.info("Starting scoringData validation");

        EmploymentDto employment = scoringDataDto.getEmployment();
        EmploymentStatus status = employment.getEmploymentStatus();
        EmploymentPosition employmentPosition = employment.getEmploymentPosition();

        if (status == EmploymentStatus.UNEMPLOYED) {
            throw new InvalidEmploymentStatusOrPositionException("Invalid employment status: " + status);
        }

        if (status == EmploymentStatus.SELF_EMPLOYED &&
                (employmentPosition == EmploymentPosition.TOP_MANAGER || employmentPosition == EmploymentPosition.WORKER)) {
            throw new InvalidEmploymentStatusOrPositionException("Self-employed cannot have TOP_MANAGER or WORKER employmentPosition.");
        }
        if (status == EmploymentStatus.BUSINESS_OWNER && employmentPosition != EmploymentPosition.OWNER) {
            throw new InvalidEmploymentStatusOrPositionException("Business owner must have OWNER employmentPosition.");
        }

        if(scoringDataDto.getAmount().compareTo(employment.getSalary().multiply(MONTH_AMOUNT_IN_2_YEARS)) > 0) {
            throw new InvalidCreditAmountException("Credit amount exceeds amount of salary per 2 years");
        }

        log.info("Validation passed for employment status: {} and employmentPosition: {}", status, employmentPosition);
    }
}