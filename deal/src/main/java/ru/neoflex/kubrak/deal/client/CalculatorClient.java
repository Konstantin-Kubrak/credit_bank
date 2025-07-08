package ru.neoflex.kubrak.deal.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.neoflex.kubrak.deal.dto.CreditDto;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.dto.ScoringDataDto;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.exception.CreditRequestFailedException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorClient {

    private final RestClient restClient;
    @Value("${calculator.service.calc-offers-url}")
    private String offersUrl;
    @Value("${calculator.service.calc-credit-url}")
    private String calcCreditUrl;

    public List<LoanOfferDto> getOffers(LoanStatementRequestDto requestDto) {

        log.info("Sending request to calculator service ({}) with loan statement request", offersUrl);
        log.debug("LoanStatementRequestDto: {}", requestDto);

        return restClient.post()
                .uri(offersUrl)
                .body(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new CalculatorServiceException("Calculator service returned error: "
                            + response.getStatusCode() + "\n, returned message: " + response);
                })
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {
                });

    }

    public CreditDto getCredit(ScoringDataDto scoringData) throws CreditRequestFailedException {

        log.info("Sending request to calculator service ({}) with scoring data", calcCreditUrl);
        CreditDto creditDto = restClient.post()
                .uri(calcCreditUrl)
                .body(scoringData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new CalculatorServiceException("Calculator service returned error: "
                            + response.getStatusCode() + "\n, returned message: " + response);
                })
                .body(CreditDto.class);
        log.debug("Received response from calculator service");
        if (creditDto == null) {
            log.error("Failed to get credit calculation for scoringData: {}", scoringData);
            throw new CreditRequestFailedException("Credit calculation failed, received NULL from ms calculator");
        } else return creditDto;
    }
}
