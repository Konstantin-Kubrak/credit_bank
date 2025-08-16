package ru.neoflex.kubrak.gateway.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.neoflex.kubrak.gateway.dto.FinishRegistrationRequestDto;
import ru.neoflex.kubrak.gateway.exception.DealServiceException;
import ru.neoflex.kubrak.gateway.model.entity.Statement;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealClient {

    private final RestClient dealRestClient;

    @Value("${client.service.deal.endpoints.calculate}")
    private String calculateEndpoint;

    @Value("${client.service.deal.endpoints.send-documents}")
    private String sendDocumentsEndpoint;

    @Value("${client.service.deal.endpoints.sign-documents}")
    private String signDocumentsEndpoint;

    @Value("${client.service.deal.endpoints.verify-code}")
    private String verifyCodeEndpoint;

    @Value("${client.service.deal.endpoints.get-statement}")
    private String getStatementEndpoint;

    @Value("${client.service.deal.endpoints.get-all-statements}")
    private String getAllStatementsEndpoint;

    public void finishCreditRegistration(UUID statementId, FinishRegistrationRequestDto requestDto) {
        log.info("Sending finish registration request to deal service");

        dealRestClient.post()
                .uri(calculateEndpoint, statementId)
                .body(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new DealServiceException(
                            calculateEndpoint,
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            response.getStatusText()
                    );
                })
                .toBodilessEntity();
    }

    public void createDocuments(UUID statementId) {
        log.info("Sending create documents request to deal service");

        dealRestClient.post()
                .uri(sendDocumentsEndpoint, statementId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new DealServiceException(
                            sendDocumentsEndpoint,
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            response.getStatusText()
                    );
                })
                .toBodilessEntity();
    }

    public void signDocuments(UUID statementId) {
        log.info("Sending sign documents request to deal service");

        dealRestClient.post()
                .uri(signDocumentsEndpoint, statementId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new DealServiceException(
                            signDocumentsEndpoint,
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            response.getStatusText()
                    );
                })
                .toBodilessEntity();
    }

    public void verifyCode(UUID statementId) {
        log.info("Sending verify code request to deal service");

        dealRestClient.post()
                .uri(verifyCodeEndpoint, statementId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new DealServiceException(
                            verifyCodeEndpoint,
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            response.getStatusText()
                    );
                })
                .toBodilessEntity();
    }

    public Statement getStatementById(UUID statementId) {
        log.info("Receiving statement with ID: {}", statementId);

        return dealRestClient.get()
                .uri(getStatementEndpoint, statementId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new DealServiceException(
                            getStatementEndpoint,
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            response.getStatusText()
                    );
                })
                .body(Statement.class);
    }

    public List<Statement> getAllStatements() {
        log.info("Receiving all statements");

        return dealRestClient.get()
                .uri(getAllStatementsEndpoint)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new DealServiceException(
                            getAllStatementsEndpoint,
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            response.getStatusText()
                    );
                })
                .body(new ParameterizedTypeReference<List<Statement>>() {});
    }
}