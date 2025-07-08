package ru.neoflex.kubrak.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import ru.neoflex.kubrak.deal.client.CalculatorClient;
import ru.neoflex.kubrak.deal.dto.*;
import ru.neoflex.kubrak.deal.dto.dtoMapper.CreditMapper;
import ru.neoflex.kubrak.deal.dto.dtoMapper.EmploymentMapper;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.exception.CreditRequestFailedException;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Credit;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.model.enums.ChangeType;
import ru.neoflex.kubrak.deal.model.enums.CreditStatus;
import ru.neoflex.kubrak.deal.model.jsonb.Passport;
import ru.neoflex.kubrak.deal.repository.ClientRepository;
import ru.neoflex.kubrak.deal.repository.CreditRepository;
import ru.neoflex.kubrak.deal.repository.StatementRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DealService {

    private final StatementService statementService;
    private final ClientService clientService;
    private final EmploymentMapper employmentMapper;
    private final CreditMapper creditMapper;
    private final CreditRepository creditRepository;
    private final CalculatorClient calculatorClient;
    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;

    public List<LoanOfferDto> getLoanOfferList(LoanStatementRequestDto loanStatementRequestDto) {

        log.info("Getting loan offers for request. Client email: {}", loanStatementRequestDto.getEmail());
        Client client = clientService.createClient(loanStatementRequestDto);
        log.debug("Client created: {}", client);

        clientRepository.save(client);
        log.debug("Client saved to DB");

        Statement statement = statementService.createStatement(client);
        statementRepository.save(statement);
        log.debug("Statement created and saved: {}", statement);

        List<LoanOfferDto> loanOfferDtoList = calculatorClient.getOffers(loanStatementRequestDto);
        if (loanOfferDtoList == null || loanOfferDtoList.isEmpty()) {
            throw new CalculatorServiceException("No loan offers received from the calculator client");
        }

        log.debug("Received {} offers from calculator: {}", loanOfferDtoList.size(), loanOfferDtoList);
        for (
                LoanOfferDto loanOfferDto : loanOfferDtoList) {
            loanOfferDto.setStatementId(statement.getStatementId());
        }
        log.info("Successfully processed loan offers request. Generated {} offers", loanOfferDtoList.size());

        return loanOfferDtoList;
    }

    @Transactional
    public void finishCreditRegistration(UUID statementId, FinishRegistrationRequestDto frrDto) {

        log.info("Starting credit registration for statement ID: {}", statementId);

        Statement statement = statementRepository
                .findById(statementId)
                .orElseThrow(()-> new StatementNotFoundException(statementId));
        if (statement == null) {
            throw new StatementNotFoundException(statementId);
        }
        log.debug("Retrieved statement: {}", statement.getStatementId());
        Client client = statement.getClient();
        client = clientService.completeClientData(client, frrDto);
        log.debug("Client data completed for: {}", client.getClientId());

        ScoringDataDto scoringData = createScoringDataDto(statement);
        log.debug("Scoring data prepared: {}", scoringData);

        CreditDto creditDto;
        try {
            creditDto = calculatorClient.getCredit(scoringData);
            if (creditDto == null) {
                throw new CreditRequestFailedException("No credit received from the calculator client");
            }
        } catch (RestClientException ex) {
            throw new CalculatorServiceException("Failed to call calculator service: " + ex.getMessage());
        }
        log.debug("Received credit calculation: {}", creditDto);

        Credit credit = creditMapper.toEntity(creditDto);
        credit.setCreditStatus(CreditStatus.CALCULATED);
        creditRepository.save(credit);
        log.debug("Credit entity created: {}", credit.getCreditId());

        statement.setCredit(credit);
        statementService.updateStatement(statement, ApplicationStatus.CC_APPROVED, ChangeType.AUTOMATIC);
        log.info("Successfully finished credit registration for statement ID: {}", statementId);
    }

    public ScoringDataDto createScoringDataDto(Statement statement) {

        Client client = statement.getClient();
        Passport passport = client.getPassport();
        Credit credit = statement.getCredit();
        EmploymentDto employmentDto = employmentMapper.toDto(client.getEmployment());

        return new ScoringDataDto()
                .setAmount(credit.getAmount())
                .setTerm(credit.getTerm())
                .setFirstName(client.getFirstName())
                .setLastName(client.getLastName())
                .setMiddleName(client.getMiddleName())
                .setGender(client.getGender())
                .setBirthdate(client.getBirthDate())
                .setPassportSeries(passport.getSeries())
                .setPassportNumber(passport.getNumber())
                .setPassportIssueDate(passport.getIssueDate())
                .setPassportIssueBranch(passport.getIssueBranch())
                .setMaritalStatus(client.getMaritalStatus())
                .setDependentAmount(client.getDependentAmount())
                .setEmployment(employmentDto)
                .setAccountNumber(client.getAccountNumber())
                .setIsInsuranceEnabled(credit.getInsuranceEnabled())
                .setIsSalaryClient(credit.getSalaryClient());
    }
}