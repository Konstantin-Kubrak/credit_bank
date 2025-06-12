package ru.neoflex.kubrak.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.calculator.dto.LoanOfferDto;
import ru.neoflex.kubrak.calculator.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalcService {

    @Value("${credit.base-rate}")
    private BigDecimal baseRate;
    @Value("${credit.rate.adjustment.insurance}")
    private BigDecimal insuranceRateAdjustment;
    @Value("${credit.rate.adjustment.salary-client}")
    private BigDecimal salaryClientRateAdjustment;
    @Value("${credit.rate.minimum}")
    private BigDecimal minimumRate;

    private final BaseCalculationsService baseCalc;

    public List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request) {

        log.info("Starting loan offers calculation for request: {}", request);

        List<LoanOfferDto> offers = new ArrayList<>();

        offers.add(createOffer(request, false, false));
        offers.add(createOffer(request, false, true));
        offers.add(createOffer(request, true, false));
        offers.add(createOffer(request, true, true));

        offers.sort((o1, o2) -> o2.getRate().compareTo(o1.getRate()));
        log.info("Generated {} offers, sorted from worst to best: {}", offers.size(), offers);

        return offers;
    }

    public LoanOfferDto createOffer(LoanStatementRequestDto request, Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        log.info("Creating offer for request: {}, isInsuranceEnabled: {}, isSalaryClient: {}",
                request, isInsuranceEnabled, isSalaryClient);

        LoanOfferDto offer = new LoanOfferDto()
                .setStatementId(UUID.randomUUID())
                .setRequestedAmount(request.getAmount())
                .setTerm(request.getTerm())
                .setIsInsuranceEnabled(isInsuranceEnabled)
                .setIsSalaryClient(isSalaryClient);

        BigDecimal rate = baseRate;
        log.debug("Initial base rate: {}", rate);

        if (isInsuranceEnabled) {
            rate = rate.add(insuranceRateAdjustment);
            log.debug("After insurance adjustment (+{}): {}", insuranceRateAdjustment, rate);
        }
        if (isSalaryClient) {
            rate = rate.add(salaryClientRateAdjustment);
            log.debug("After salary client adjustment (+{}): {}", salaryClientRateAdjustment, rate);
        }

        BigDecimal totalAmount = request.getAmount();
        log.debug("Initial total amount: {}", totalAmount);

        if (isInsuranceEnabled) {
            BigDecimal insuranceCost = baseCalc.calculateInsuranceCost(totalAmount);
            log.debug("Calculated insurance cost: {}", insuranceCost);
            totalAmount = totalAmount.add(insuranceCost);
            log.debug("Total amount with insurance: {}", totalAmount);
        }

        rate = rate.max(minimumRate);
        log.debug("Final rate after minimum rate check (min {}): {}", minimumRate, rate);

        BigDecimal monthlyPayment = baseCalc.calculateMonthlyPayment(totalAmount, rate, request.getTerm());
        log.debug("Calculated monthly payment: {}", monthlyPayment);

        offer.setTotalAmount(totalAmount)
                .setRate(rate)
                .setMonthlyPayment(monthlyPayment);
        log.info("Created offer: {}", offer);

        return offer;
    }
}
