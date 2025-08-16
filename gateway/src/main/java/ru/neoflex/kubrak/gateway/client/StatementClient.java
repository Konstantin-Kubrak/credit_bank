package ru.neoflex.kubrak.gateway.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.neoflex.kubrak.gateway.dto.LoanOfferDto;
import ru.neoflex.kubrak.gateway.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.gateway.exception.StatementServiceException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementClient {

    private final RestClient statementRestClient;

    @Value("${client.service.statement.endpoints.get-offers}")
    private String getOffersEndpoint;

    @Value("${client.service.statement.endpoints.select-offer}")
    private String selectOfferEndpoint;

    public List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto requestDto) {
        log.info("Sending request to statement service for loan offers");

        return statementRestClient.post()
                .uri(getOffersEndpoint)
                .body(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new StatementServiceException(
                            getOffersEndpoint,
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            response.getStatusText()
                    );
                })
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});
    }

    public void selectOffer(LoanOfferDto offerDto) {
        log.info("Sending selected offer to statement service");

        statementRestClient.post()
                .uri(selectOfferEndpoint)
                .body(offerDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new StatementServiceException(
                            selectOfferEndpoint,
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            response.getStatusText()
                    );
                })
                .toBodilessEntity();
    }
}