package ru.neoflex.kubrak.statement.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.statement.client.DealClient;
import ru.neoflex.kubrak.statement.dto.LoanOfferDto;
import ru.neoflex.kubrak.statement.dto.LoanStatementRequestDto;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class StatementService {

    private final ValidationService validationService;
    private final DealClient dealClient;

    public List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto lsrDto) {
        log.info("Starting loan offers processing for request with email: {}", lsrDto.getEmail());
        log.debug("Incoming LoanStatementRequestDto details: {}", lsrDto);

        log.debug("Performing pre-scoring validation...");
        validationService.preScoring(lsrDto);
        log.debug("Pre-scoring validation passed successfully");

        log.debug("Sending request to Deal service for offers calculation...");
        List<LoanOfferDto> offers = dealClient.getOffers(lsrDto);
        log.debug("Received {} offers from Deal service", offers.size());

        if (log.isDebugEnabled()) {
            offers.forEach(offer ->
                    log.debug("Offer details - ID: {}, Amount: {}, Term: {}, Rate: {}",
                            offer.getStatementId(),
                            offer.getRequestedAmount(),
                            offer.getTerm(),
                            offer.getRate()));
        }

        log.info("Successfully processed loan offers request for email: {}. Generated {} offers",
                lsrDto.getEmail(), offers.size());

        return offers;
    }


    public void selectOffer(LoanOfferDto loanOfferDto) {
        log.info("Starting offer selection process for statement ID: {}",
                loanOfferDto.getStatementId());
        log.debug("Incoming LoanOfferDto details: {}", loanOfferDto);

        log.debug("Validating selected offer...");
        validationService.validateLoanOffer(loanOfferDto);
        log.debug("Offer validation passed successfully");

        log.debug("Sending selected offer to Deal service...");
        dealClient.selectOfferUrl(loanOfferDto);

        log.info("Offer with ID {} successfully processed and sent to Deal service",
                loanOfferDto.getStatementId());
    }
}
