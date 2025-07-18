package ru.neoflex.kubrak.statement.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.neoflex.kubrak.statement.dto.LoanOfferDto;
import ru.neoflex.kubrak.statement.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.statement.exception.DealServiceException;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealClient {

    private final RestClient restClient;
    @Value("${deal.service.get-loan-offers-url}")
    private String getLoanOffersUrl;
    @Value("${deal.service.select-offer-url}")
    private String selectOfferUrl;

    public List<LoanOfferDto> getOffers(LoanStatementRequestDto requestDto) {

        log.info("Sending request to deal service ({}) with loan statement request", getLoanOffersUrl);
        log.debug("LoanStatementRequestDto: {}", requestDto);

        return restClient.post()
                .uri(getLoanOffersUrl)
                .body(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new DealServiceException("Deal service returned error: "
                            + response.getStatusCode() + "\n, returned message: " + response);
                })
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {
                });

    }

    public void selectOfferUrl(LoanOfferDto loanOfferDto){

        log.info("Sending request to calculator service ({}) with scoring data", selectOfferUrl);
        restClient.post()
                .uri(selectOfferUrl)
                .body(loanOfferDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new DealServiceException("Calculator service returned error: "
                            + response.getStatusCode() + "\n, returned message: " + response);
                });
        log.debug("Received response from calculator service");
    }
}
