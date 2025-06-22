package ru.neoflex.kubrak.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.dto.dtoMapper.EmploymentMapper;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.jsonb.Employment;
import ru.neoflex.kubrak.deal.model.jsonb.Passport;
import ru.neoflex.kubrak.deal.repository.ClientRepository;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final EmploymentMapper employmentMapper;

    public void saveClient(Client client) {

        log.info("Saving client with email: {}", client.getEmail());
        clientRepository.save(client);
        log.debug("Client saved successfully. ID: {}", client.getClientId());
    }

    public Client createClient(LoanStatementRequestDto lsrDto) {

        log.info("Creating new client from request. Email: {}", lsrDto.getEmail());
        log.debug("LoanStatementRequestDto: {}", lsrDto);
        Passport passport = new Passport()
                .setSeries(lsrDto.getPassportSeries())
                .setNumber(lsrDto.getPassportNumber());

        Client client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName(lsrDto.getFirstName())
                .lastName(lsrDto.getLastName())
                .middleName(lsrDto.getMiddleName())
                .birthDate(lsrDto.getBirthdate())
                .email(lsrDto.getEmail())
                .gender(null)
                .maritalStatus(null)
                .passport(passport)
                .dependentAmount(0)
                .accountNumber(UUID.randomUUID().toString())
                .build();
        log.info("Client created successfully. ID: {}", client.getClientId());

        return client;
    }

    public Client completeClientData(Client client, FinishRegistrationRequestDto frrDto) {

        log.info("Completing client data for client ID: {}", client.getClientId());
        log.debug("Received completion data (FinishRegistrationRequestDto): {}", frrDto);
                Employment employment = employmentMapper.toEntity(frrDto.getEmployment());

        client.getPassport()
                .setIssueBranch(frrDto.getPassportIssueBranch())
                .setIssueDate(frrDto.getPassportIssueDate());
        client
                .setGender(frrDto.getGender())
                .setMaritalStatus(frrDto.getMaritalStatus())
                .setDependentAmount(frrDto.getDependentAmount())
                .setEmployment(employment)
                .setAccountNumber(frrDto.getAccountNumber());

        log.debug("Client completed successfully. Client data: {}", client);
        log.info("Client data completed successfully for ID: {}", client.getClientId());
        return client;
    }

}
