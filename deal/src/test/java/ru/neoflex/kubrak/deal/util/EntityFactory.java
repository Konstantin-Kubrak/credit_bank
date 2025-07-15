package ru.neoflex.kubrak.deal.util;

import ru.neoflex.kubrak.deal.dto.*;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Credit;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.model.enums.CreditStatus;
import ru.neoflex.kubrak.deal.model.enums.Gender;
import ru.neoflex.kubrak.deal.model.enums.MaritalStatus;
import ru.neoflex.kubrak.deal.model.jsonb.Employment;
import ru.neoflex.kubrak.deal.model.jsonb.Passport;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EntityFactory {


    public static List<LoanOfferDto> createExpectedOffers() {
        return List.of(
                new LoanOfferDto()
                        .setStatementId(UUID.randomUUID())
                        .setRequestedAmount(BigDecimal.valueOf(100000))
                        .setTotalAmount(BigDecimal.valueOf(120000))
                        .setTerm(12)
                        .setMonthlyPayment(BigDecimal.valueOf(10000))
                        .setRate(BigDecimal.valueOf(10))
                        .setIsInsuranceEnabled(true)
                        .setIsSalaryClient(false),
                new LoanOfferDto()
                        .setStatementId(UUID.randomUUID())
                        .setRequestedAmount(BigDecimal.valueOf(100000))
                        .setTotalAmount(BigDecimal.valueOf(110000))
                        .setTerm(12)
                        .setMonthlyPayment(BigDecimal.valueOf(9166.67))
                        .setRate(BigDecimal.valueOf(8))
                        .setIsInsuranceEnabled(false)
                        .setIsSalaryClient(true)
        );
    }

    public static LoanStatementRequestDto createTestLoanRequest() {

        return new LoanStatementRequestDto()
                .setAmount(BigDecimal.valueOf(100000))
                .setTerm(12)
                .setFirstName("Ivan")
                .setLastName("Ivanov")
                .setEmail("Ivan.Ivanov@example.com")
                .setBirthdate(LocalDate.of(1990, 1, 1))
                .setPassportSeries("1234")
                .setPassportNumber("567890");
    }


    public static FinishRegistrationRequestDto createTestFinishRegistrationRequestDto() {

        return new FinishRegistrationRequestDto()
                .setGender(Gender.MALE)
                .setMaritalStatus(MaritalStatus.MARRIED)
                .setDependentAmount(2)
                .setPassportIssueDate(LocalDate.now())
                .setPassportIssueBranch("UFMS-123")
                .setEmployment(new EmploymentDto())
                .setAccountNumber("1234567890");
    }

    public static Statement createTestStatement(UUID statementId) {

        Client client = createTestClient();
        Credit credit = createTestCredit();

        return Statement.builder()
                .statementId(statementId)
                .creationDate(Timestamp.valueOf(LocalDateTime.now()))
                .client(client)
                .credit(credit)
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .status(ApplicationStatus.PREAPPROVAL)
                .build();
    }

    public static Client createTestClient() {

        Random random = new Random();
        return Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .email(UUID.randomUUID().toString().substring(0,6) + "@test.com")
                .passport(new Passport()
                        .setSeries("1234")
                        .setNumber("567890")
                        .setIssueDate(LocalDate.now())
                        .setIssueBranch("UFMS-123"))
                .employment(Employment.builder().build())
                .accountNumber(String.format("%d%09d",
                                1 + random.nextInt(9),
                                random.nextInt(1_000_000_000)))
                .build();
    }

    public static CreditDto createTestCreditDto() {

        return new CreditDto()
                .setAmount(BigDecimal.valueOf(100000))
                .setTerm(12)
                .setMonthlyPayment(BigDecimal.valueOf(10000))
                .setRate(BigDecimal.valueOf(10))
                .setPsk(BigDecimal.valueOf(120000))
                .setIsInsuranceEnabled(true)
                .setIsSalaryClient(false)
                .setPaymentSchedule(List.of(createPaymentScheduleElementDto()));
    }

    public static PaymentScheduleElementDto createPaymentScheduleElementDto() {

        PaymentScheduleElementDto paymentElement = new PaymentScheduleElementDto();
        paymentElement.setNumber(1);
        paymentElement.setDate(LocalDate.now().plusMonths(1));
        paymentElement.setTotalPayment(BigDecimal.valueOf(10000));
        paymentElement.setInterestPayment(BigDecimal.valueOf(2000));
        paymentElement.setDebtPayment(BigDecimal.valueOf(8000));
        paymentElement.setRemainingDebt(BigDecimal.valueOf(92000));

        return paymentElement;
    }

    public static Credit createTestCredit() {

        return Credit.builder()
                .creditId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(100000))
                .term(12)
                .psk(BigDecimal.valueOf(5))
                .rate(BigDecimal.valueOf(10))
                .monthlyPayment(BigDecimal.valueOf(10000))
                .insuranceEnabled(true)
                .creditStatus(CreditStatus.CALCULATED)
                .salaryClient(false)
                .build();
    }


}
