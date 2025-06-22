package ru.neoflex.kubrak.deal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.kubrak.deal.dto.EmploymentDto;
import ru.neoflex.kubrak.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.dto.dtoMapper.EmploymentMapper;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.enums.EmploymentPosition;
import ru.neoflex.kubrak.deal.model.enums.EmploymentStatus;
import ru.neoflex.kubrak.deal.model.enums.Gender;
import ru.neoflex.kubrak.deal.model.enums.MaritalStatus;
import ru.neoflex.kubrak.deal.model.jsonb.Employment;
import ru.neoflex.kubrak.deal.model.jsonb.Passport;
import ru.neoflex.kubrak.deal.repository.ClientRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmploymentMapper employmentMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    void saveClient_ShouldCallRepositorySave() {

        Client client = new Client();
        client.setClientId(UUID.randomUUID());
        client.setEmail("test@example.com");

        clientService.saveClient(client);

        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void createClient_ShouldCreateClientFromLoanStatementRequest() {

        LoanStatementRequestDto requestDto = new LoanStatementRequestDto()
                .setAmount(BigDecimal.valueOf(100000))
                .setTerm(12)
                .setFirstName("Ivan")
                .setLastName("Ivanov")
                .setMiddleName("Ivanovich")
                .setEmail("Ivan.Ivanov@example.com")
                .setBirthdate(LocalDate.of(1990, 1, 1))
                .setPassportSeries("1234")
                .setPassportNumber("567890");


        Client result = clientService.createClient(requestDto);

        assertNotNull(result);
        assertNotNull(result.getClientId());
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("Ivanovich", result.getMiddleName());
        assertEquals(LocalDate.of(1990, 1, 1), result.getBirthDate());
        assertEquals("Ivan.Ivanov@example.com", result.getEmail());
        assertNull(result.getGender());
        assertNull(result.getMaritalStatus());
        assertEquals(0, result.getDependentAmount());
        assertNotNull(result.getAccountNumber());

        Passport passport = result.getPassport();
        assertNotNull(passport);
        assertEquals("1234", passport.getSeries());
        assertEquals("567890", passport.getNumber());
    }

    @Test
    void completeClientData_ShouldUpdateClientWithFinishRegistrationData() {

        UUID clientId = UUID.randomUUID();
        Client existingClient = Client.builder()
                .clientId(clientId)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("Ivan.Ivanov@example.com")
                .passport(new Passport().setSeries("1234").setNumber("567890"))
                .build();

        EmploymentDto employmentDto = new EmploymentDto()
                .setEmploymentStatus(EmploymentStatus.EMPLOYED)
                .setEmployerINN("1234567890")
                .setSalary(BigDecimal.valueOf(100000))
                .setEmploymentPosition(EmploymentPosition.MANAGER)
                .setWorkExperienceTotal(60)
                .setWorkExperienceCurrent(24);

        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto()
                .setGender(Gender.MALE)
                .setMaritalStatus(MaritalStatus.MARRIED)
                .setDependentAmount(2)
                .setPassportIssueDate(LocalDate.of(2015, 5, 15))
                .setPassportIssueBranch("UFMS-123")
                .setEmployment(employmentDto)
                .setAccountNumber("12345678901234567890");

        Employment expectedEmployment = Employment.builder().build();
        when(employmentMapper.toEntity(employmentDto)).thenReturn(expectedEmployment);

        Client result = clientService.completeClientData(existingClient, requestDto);

        assertSame(existingClient, result);
        assertEquals(Gender.MALE, result.getGender());
        assertEquals(MaritalStatus.MARRIED, result.getMaritalStatus());
        assertEquals(2, result.getDependentAmount());
        assertEquals("12345678901234567890", result.getAccountNumber());

        Passport passport = result.getPassport();
        assertEquals("UFMS-123", passport.getIssueBranch());
        assertEquals(LocalDate.of(2015, 5, 15), passport.getIssueDate());

        assertSame(expectedEmployment, result.getEmployment());
        verify(employmentMapper, times(1)).toEntity(employmentDto);
    }

    @Test
    void completeClientData_ShouldNotChangeClientIdAndBasicInfo() {

        UUID clientId = UUID.randomUUID();
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Client existingClient = Client.builder()
                .clientId(clientId)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .birthDate(birthDate)
                .email("Ivan.Ivanov@example.com")
                .passport(new Passport().setSeries("1234").setNumber("567890"))
                .build();

        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto()
                .setGender(Gender.MALE)
                .setMaritalStatus(MaritalStatus.MARRIED)
                .setDependentAmount(2)
                .setPassportIssueDate(LocalDate.of(2015, 5, 15))
                .setPassportIssueBranch("UFMS-123")
                .setEmployment(new EmploymentDto())
                .setAccountNumber("12345678901234567890");

        when(employmentMapper.toEntity(any())).thenReturn(Employment.builder().build());

        Client result = clientService.completeClientData(existingClient, requestDto);

        assertEquals(clientId, result.getClientId());
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("Ivanovich", result.getMiddleName());
        assertEquals(birthDate, result.getBirthDate());
        assertEquals("Ivan.Ivanov@example.com", result.getEmail());
    }
}