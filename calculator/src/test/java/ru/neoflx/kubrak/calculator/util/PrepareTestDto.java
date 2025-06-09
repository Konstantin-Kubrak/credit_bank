package ru.neoflx.kubrak.calculator.util;

import ru.neoflx.kubrak.calculator.dto.EmploymentDto;
import ru.neoflx.kubrak.calculator.dto.LoanStatementRequestDto;
import ru.neoflx.kubrak.calculator.dto.ScoringDataDto;
import ru.neoflx.kubrak.calculator.model.enums.EmploymentStatus;
import ru.neoflx.kubrak.calculator.model.enums.Gender;
import ru.neoflx.kubrak.calculator.model.enums.MaritalStatus;
import ru.neoflx.kubrak.calculator.model.enums.Position;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PrepareTestDto {

    public static LoanStatementRequestDto createValidLsrDto() {

        LoanStatementRequestDto dto = new LoanStatementRequestDto();
        dto.setAmount(BigDecimal.valueOf(100000));
        dto.setTerm(12);
        dto.setFirstName("Ivan");
        dto.setLastName("Ivanov");
        dto.setMiddleName("Ivanovich");
        dto.setEmail("ivanov@example.com");
        dto.setBirthdate(LocalDate.now().minusYears(25));
        dto.setPassportSeries("1234");
        dto.setPassportNumber("123456");

        return dto;
    }

    public static ScoringDataDto createValidScoringDataDto() {

        ScoringDataDto dto = new ScoringDataDto();

        dto.setAmount(BigDecimal.valueOf(100000));
        dto.setTerm(12);
        dto.setFirstName("Ivan");
        dto.setLastName("Ivanov");
        dto.setMiddleName("Ivanovich");
        dto.setGender(Gender.MALE);
        dto.setBirthdate(LocalDate.now().minusYears(25));
        dto.setPassportSeries("1234");
        dto.setPassportNumber("123456");
        dto.setPassportIssueDate(LocalDate.now().minusYears(1));
        dto.setPassportIssueBranch("Branch 123");
        dto.setMaritalStatus(MaritalStatus.SINGLE);
        dto.setDependentAmount(0);
        dto.setEmployment(createValidEmploymentDto());
        dto.setAccountNumber("1234567890");
        dto.setIsInsuranceEnabled(false);
        dto.setIsSalaryClient(false);

        return dto;
    }

    public static EmploymentDto createValidEmploymentDto() {

        EmploymentDto dto = new EmploymentDto();
        dto.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        dto.setEmployerINN("123456789012");
        dto.setSalary(BigDecimal.valueOf(50000));
        dto.setPosition(Position.MANAGER);
        dto.setWorkExperienceTotal(60);
        dto.setWorkExperienceCurrent(24);

        return dto;
    }
}
