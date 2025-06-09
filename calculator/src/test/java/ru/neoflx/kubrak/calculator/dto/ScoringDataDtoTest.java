package ru.neoflx.kubrak.calculator.dto;


import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.neoflx.kubrak.calculator.util.PrepareTestDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("Модульное тестирование - валидация ScoringDataDto:")
class ScoringDataDtoTest {

    @Autowired
    private LocalValidatorFactoryBean validator;

    ScoringDataDto dto;
    
    @BeforeEach
    void setUp() {
        
        dto = PrepareTestDto.createValidScoringDataDto();
    }

    @Test
    void whenScoringDataValid_thenNoViolations() {
        
        Set<ConstraintViolation<ScoringDataDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidScoringAmountProvider")
    void whenScoringAmountInvalid_thenValidationFails(BigDecimal amount, String expectedMessage) {
        
        dto.setAmount(amount);
        assertSingleViolation(dto, expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("invalidTermProvider")
    void whenTermInvalid_thenValidationFails(Integer term, String expectedMessage) {
        
        dto.setTerm(term);
        assertSingleViolation(dto, expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("invalidNameProvider")
    void whenScoringNameInvalid_thenValidationFails(String fieldName, String value, String expectedMessage) {
        
        switch (fieldName) {
            case "firstName" -> dto.setFirstName(value);
            case "lastName" -> dto.setLastName(value);
            case "middleName" -> dto.setMiddleName(value);
        }
        assertSingleViolation(dto, expectedMessage);
    }

    @Test
    void whenGenderNull_thenValidationFails() {
        
        dto.setGender(null);
        assertSingleViolation(dto, "The gender must be specified.");
    }


    @Test
    void whenBirthdateNotAdultOrNull_thenValidationFails() {
        
        dto.setBirthdate(LocalDate.now().minusYears(17));
        assertSingleViolation(dto, "Birthdate cannot be null and age must be between 18 and 60");
    }

    @ParameterizedTest
    @MethodSource("invalidPassportProvider")
    void whenPassportInvalid_thenValidationFails(String fieldName, String value, String expectedMessage) {
        
        switch (fieldName) {
            case "series" -> dto.setPassportSeries(value);
            case "number" -> dto.setPassportNumber(value);
        }
        assertSingleViolation(dto, expectedMessage);
    }

    @Test
    void whenPassportIssueDateBeforeMin_thenValidationFails() {
        
        dto.setPassportIssueDate(LocalDate.of(1997, 9, 30));
        assertSingleViolation(dto, "RF passport cannot be issued before 1997-10-01");
    }

    @Test
    void whenMaritalStatusNull_thenValidationFails() {
        
        dto.setMaritalStatus(null);
        assertSingleViolation(dto, "Marital status cannot be null");
    }

    @ParameterizedTest
    @MethodSource("invalidDependentAmountProvider")
    void whenDependentAmountInvalid_thenValidationFails(Integer amount, String expectedMessage) {
        
        dto.setDependentAmount(amount);
        assertSingleViolation(dto, expectedMessage);
    }

    @Test
    void whenEmploymentNull_thenValidationFails() {
        
        dto.setEmployment(null);
        assertSingleViolation(dto, "Employment cannot be null");
    }

    @Test
    void whenAccountNumberBlank_thenValidationFails() {
        
        dto.setAccountNumber("");
        assertSingleViolation(dto, "Account number cannot be blank");
    }

    @Test
    void whenInsuranceStatusNull_thenValidationFails() {
        
        dto.setIsInsuranceEnabled(null);
        assertSingleViolation(dto, "Insurance status must be specified");
    }

    @Test
    void whenSalaryClientStatusNull_thenValidationFails() {
        
        dto.setIsSalaryClient(null);
        assertSingleViolation(dto, "Salary client status must be specified");
    }


    // ========== Test Data Providers ==========

    private static Stream<Arguments> invalidScoringAmountProvider() {
        return Stream.of(
                Arguments.of(null, "Amount cannot be null"),
                Arguments.of(BigDecimal.ZERO, "Amount must be greater than 0"),
                Arguments.of(BigDecimal.valueOf(-1), "Amount must be greater than 0")
        );
    }

    private static Stream<Arguments> invalidTermProvider() {
        return Stream.of(
                Arguments.of(null, "Term cannot be null"),
                Arguments.of(0, "Term must be at least 1"),
                Arguments.of(61, "Term cannot be more than 60")
        );
    }

    private static Stream<Arguments> invalidNameProvider() {

        return Stream.of(
                Arguments.of("firstName", null, "First name cannot be null"),
                Arguments.of("firstName", "", "Invalid first name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("firstName", "A", "Invalid first name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("firstName", "Name1", "Invalid first name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("firstName", "VeryLongFirstNameThatExceedsLimit", "Invalid first name, must contain only letters and length must be between 2 and 20 chars"),

                Arguments.of("lastName", null, "Last name cannot be null"),
                Arguments.of("lastName", "", "Invalid last name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("lastName", "A", "Invalid last name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("lastName", "Name1", "Invalid last name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("lastName", "VeryLongLastNameThatExceedsLimit", "Invalid last name, must contain only letters and length must be between 2 and 20 chars"),

                Arguments.of("middleName", null, "Middle name cannot be null"),
                Arguments.of("middleName", "", "Invalid middle name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("middleName", "A", "Invalid middle name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("middleName", "Name1", "Invalid middle name, must contain only letters and length must be between 2 and 20 chars"),
                Arguments.of("middleName", "VeryLongMiddleNameThatExceedsLimit", "Invalid middle name, must contain only letters and length must be between 2 and 20 chars")
        );
    }

    private static Stream<Arguments> invalidPassportProvider() {
        return Stream.of(
                Arguments.of("series", null, "Passport series cannot be blank"),
                Arguments.of("series", "123", "Passport series must be 4 digits"),
                Arguments.of("series", "abcd", "Passport series must be 4 digits"),
                Arguments.of("number", null, "Passport number cannot be blank"),
                Arguments.of("number", "12345", "Passport number must be 6 digits"),
                Arguments.of("number", "abcdef", "Passport number must be 6 digits")
        );
    }

    private static Stream<Arguments> invalidDependentAmountProvider() {
        return Stream.of(
                Arguments.of(null, "Dependent amount cannot be null"),
                Arguments.of(-1, "Dependent amount must be positive or zero")
        );
    }

    // ========== Helper Methods ==========

    private <T> void assertSingleViolation(T dto, String expectedMessage) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        assertEquals(1, violations.size(), "Expected exactly one violation");
        assertEquals(expectedMessage, violations.iterator().next().getMessage());
    }
}