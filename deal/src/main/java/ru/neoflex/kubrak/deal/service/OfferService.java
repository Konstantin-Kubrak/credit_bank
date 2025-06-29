package ru.neoflex.kubrak.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.deal.client.CalculatorClientService;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.repository.ClientRepository;
import ru.neoflex.kubrak.deal.repository.StatementRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OfferService {

    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final StatementService statementService;
    private final StatementRepository statementRepository;
    private final CalculatorClientService calculatorClientService;

    public List<LoanOfferDto> getLoanOfferList(LoanStatementRequestDto loanStatementRequestDto) {

        log.info("Getting loan offers for request. Client email: {}", loanStatementRequestDto.getEmail());
        Client client = clientService.createClient(loanStatementRequestDto);
        log.debug("Client created: {}", client);

        clientRepository.save(client);
        log.debug("Client saved to DB");

        Statement statement = statementService.createStatement(client);
        statementRepository.save(statement);
        log.debug("Statement created and saved: {}", statement);

        List<LoanOfferDto> loanOfferDtoList = calculatorClientService.getOffers(loanStatementRequestDto);
        log.debug("Received {} offers from calculator: {}", loanOfferDtoList.size(), loanOfferDtoList);
        for(LoanOfferDto loanOfferDto : loanOfferDtoList){
            loanOfferDto.setStatementId(statement.getStatementId());
        }
        log.info("Successfully processed loan offers request. Generated {} offers", loanOfferDtoList.size());

        return loanOfferDtoList;
    }
}