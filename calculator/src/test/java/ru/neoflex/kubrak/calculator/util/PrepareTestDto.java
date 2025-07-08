package ru.neoflex.kubrak.calculator.util;

import ru.neoflex.kubrak.calculator.dto.*;
import ru.neoflex.kubrak.calculator.model.enums.EmploymentStatus;
import ru.neoflex.kubrak.calculator.model.enums.Gender;
import ru.neoflex.kubrak.calculator.model.enums.MaritalStatus;
import ru.neoflex.kubrak.calculator.model.enums.EmploymentPosition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PrepareTestDto {

    public static LoanStatementRequestDto createValidLsrDto() {

        return new LoanStatementRequestDto()
                .setAmount(BigDecimal.valueOf(100000))
                .setTerm(12)
                .setFirstName("Ivan")
                .setLastName("Ivanov")
                .setMiddleName("Ivanovich")
                .setEmail("ivanov@example.com")
                .setBirthdate(LocalDate.now().minusYears(25))
                .setPassportSeries("1234")
                .setPassportNumber("123456");
    }

    public static ScoringDataDto createValidScoringDataDto() {

        return new ScoringDataDto()
                .setAmount(BigDecimal.valueOf(100000))
                .setTerm(12)
                .setFirstName("Ivan")
                .setLastName("Ivanov")
                .setMiddleName("Ivanovich")
                .setGender(Gender.MALE)
                .setBirthdate(LocalDate.now().minusYears(25))
                .setPassportSeries("1234")
                .setPassportNumber("123456")
                .setPassportIssueDate(LocalDate.now().minusYears(1))
                .setPassportIssueBranch("Branch 123")
                .setMaritalStatus(MaritalStatus.SINGLE)
                .setDependentAmount(0)
                .setEmployment(createValidEmploymentDto())
                .setAccountNumber("1234567890")
                .setIsInsuranceEnabled(false)
                .setIsSalaryClient(false);
    }

    public static EmploymentDto createValidEmploymentDto() {

        return new EmploymentDto()
                .setEmploymentStatus(EmploymentStatus.EMPLOYED)
                .setEmployerINN("123456789012")
                .setSalary(BigDecimal.valueOf(50000))
                .setEmploymentPosition(EmploymentPosition.MANAGER)
                .setWorkExperienceTotal(60)
                .setWorkExperienceCurrent(24);
    }

    public static CreditDto createCreditDto() {
        return new CreditDto()
                .setAmount(BigDecimal.valueOf(100000))
                .setTerm(12)
                .setRate(BigDecimal.valueOf(15.0))
                .setMonthlyPayment(BigDecimal.valueOf(9000))
                .setIsInsuranceEnabled(false)
                .setIsSalaryClient(false);
    }

    public static LoanOfferDto createLoanOffer() {
        return new LoanOfferDto()
                .setStatementId(UUID.randomUUID())
                .setRequestedAmount(BigDecimal.valueOf(100000))
                .setTotalAmount(BigDecimal.valueOf(110000))
                .setTerm(12)
                .setMonthlyPayment(BigDecimal.valueOf(9166.67))
                .setRate(BigDecimal.valueOf(12.0))
                .setIsInsuranceEnabled(false)
                .setIsSalaryClient(false);
    }
}
