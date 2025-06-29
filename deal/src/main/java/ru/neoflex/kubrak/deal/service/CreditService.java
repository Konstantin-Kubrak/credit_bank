package ru.neoflex.kubrak.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.neoflex.kubrak.deal.client.CalculatorClientService;
import ru.neoflex.kubrak.deal.dto.CreditDto;
import ru.neoflex.kubrak.deal.dto.EmploymentDto;
import ru.neoflex.kubrak.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.kubrak.deal.dto.ScoringDataDto;
import ru.neoflex.kubrak.deal.dto.dtoMapper.CreditMapper;
import ru.neoflex.kubrak.deal.dto.dtoMapper.EmploymentMapper;
import ru.neoflex.kubrak.deal.exception.CreditRequestFailedException;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Credit;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.model.enums.ChangeType;
import ru.neoflex.kubrak.deal.model.enums.CreditStatus;
import ru.neoflex.kubrak.deal.model.jsonb.Passport;
import ru.neoflex.kubrak.deal.repository.CreditRepository;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CreditService {

    private final StatementService statementService;
    private final ClientService clientService;
    private final EmploymentMapper employmentMapper;
    private final CreditMapper creditMapper;
    private final CreditRepository creditRepository;
    private final CalculatorClientService calculatorClientService;


    @Transactional
    public void finishCreditRegistration(UUID statementId, FinishRegistrationRequestDto frrDto) throws
            StatementNotFoundException,
            CreditRequestFailedException {

        log.info("Starting credit registration for statement ID: {}", statementId);

        Statement statement = statementService.getStatement(statementId);
        log.debug("Retrieved statement: {}", statement.getStatementId());
        Client client = statement.getClient();
        client = clientService.completeClientData(client, frrDto);
        log.debug("Client data completed for: {}", client.getClientId());

        ScoringDataDto scoringData = createScoringDataDto(statement);
        log.debug("Scoring data prepared: {}", scoringData);

        CreditDto creditDto = calculatorClientService.getCredit(scoringData);
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