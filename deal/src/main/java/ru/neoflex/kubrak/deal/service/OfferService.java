package ru.neoflex.kubrak.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Statement;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OfferService {

    private final ClientService clientService;
    private final StatementService statementService;

    private final RestClient restClient;

    public List<LoanOfferDto> getLoanOfferList(LoanStatementRequestDto loanStatementRequestDto) {

        log.info("Getting loan offers for request. Client email: {}", loanStatementRequestDto.getEmail());
        Client client = clientService.createClient(loanStatementRequestDto);
        log.debug("Client created: {}", client);

        clientService.saveClient(client);
        log.debug("Client saved to DB");

        Statement statement = statementService.createStatement(client);
        statementService.saveStatement(statement);
        log.debug("Statement created and saved: {}", statement);

        List<LoanOfferDto> loanOfferDtoList = getOffersFromCalculator(loanStatementRequestDto);
        log.debug("Received {} offers from calculator: {}", loanOfferDtoList.size(), loanOfferDtoList);
        for(LoanOfferDto loanOfferDto : loanOfferDtoList){
            loanOfferDto.setStatementId(statement.getStatementId());
        }
        log.info("Successfully processed loan offers request. Generated {} offers", loanOfferDtoList.size());

        return loanOfferDtoList;
    }

    public List<LoanOfferDto> getOffersFromCalculator(LoanStatementRequestDto requestDto)  {

        log.info("Getting loan offers from MS calculator.");
        log.debug("LoanStatementRequestDto: {}", requestDto);
        try {
            return restClient.post()
                    .uri("/calculator/offers")
                    .body(requestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new CalculatorServiceException("Calculator service returned error: "
                                + response.getStatusCode() + "\n, returned message: " + response);
                    })
                    .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});
        } catch (RestClientException e) {
            throw new CalculatorServiceException("Failed to call calculator service: " + e.getMessage());
        }
    }
}