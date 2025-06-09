package ru.neoflx.kubrak.calculator.dto;

import jakarta.validation.ConstraintViolation;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Модульное тестирование - валидация LoanStatementRequestDto:")
class LoanStatementRequestDtoTest {

    @Autowired
    private LocalValidatorFactoryBean validator;

    @Test
    void whenAllFieldsValid_thenNoViolations() {

        LoanStatementRequestDto dto = PrepareTestDto.createValidLsrDto();
        Set<ConstraintViolation<LoanStatementRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidAmountProvider")
    void whenAmountInvalid_thenValidationFails(BigDecimal amount, String expectedMessage) {

        LoanStatementRequestDto dto = PrepareTestDto.createValidLsrDto();
        dto.setAmount(amount);
        assertSingleViolation(dto, expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("invalidTermProvider")
    void whenTermInvalid_thenValidationFails(Integer term, String expectedMessage) {

        LoanStatementRequestDto dto = PrepareTestDto.createValidLsrDto();
        dto.setTerm(term);
        assertSingleViolation(dto, expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("invalidNameProvider")
    void whenNameInvalid_thenValidationFails(String fieldName, String value, String expectedMessage) {

        LoanStatementRequestDto dto = PrepareTestDto.createValidLsrDto();
        switch (fieldName) {
            case "firstName" -> dto.setFirstName(value);
            case "lastName" -> dto.setLastName(value);
            case "middleName" -> dto.setMiddleName(value);
        }
        assertSingleViolation(dto, expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("invalidEmailProvider")
    void whenEmailInvalid_thenValidationFails(String email, String expectedMessage) {

        LoanStatementRequestDto dto = PrepareTestDto.createValidLsrDto();
        dto.setEmail(email);
        assertSingleViolation(dto, expectedMessage);
    }


    @Test
    void whenBirthdateNotAdultOrNull_thenValidationFails() {
        LoanStatementRequestDto dto = PrepareTestDto.createValidLsrDto();
        dto.setBirthdate(LocalDate.now().minusYears(17));
        assertSingleViolation(dto, "Birthdate cannot be null and age must be between 18 and 60");
    }

    @ParameterizedTest
    @MethodSource("invalidPassportProvider")
    void whenPassportInvalid_thenValidationFails(String fieldName, String value, String expectedMessage) {

        LoanStatementRequestDto dto = PrepareTestDto.createValidLsrDto();
        switch (fieldName) {
            case "series" -> dto.setPassportSeries(value);
            case "number" -> dto.setPassportNumber(value);
        }
        assertSingleViolation(dto, expectedMessage);
    }

    // ========== Test Data Providers ==========

    private static Stream<Arguments> invalidAmountProvider() {

        return Stream.of(
                Arguments.of(null, "Amount cannot be null"),
                Arguments.of(BigDecimal.ZERO, "Amount must be greater than 0"),
                Arguments.of(BigDecimal.valueOf(-100), "Amount must be greater than 0")
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

    private static Stream<Arguments> invalidEmailProvider() {

        return Stream.of(
                Arguments.of(null, "Email cannot be blank"),
                Arguments.of("", "Email cannot be blank"),
                Arguments.of("invalid-email", "Email should be valid"),
                Arguments.of("user@", "Email should be valid")
        );
    }

    private static Stream<Arguments> invalidPassportProvider() {

        return Stream.of(
                Arguments.of("series", null, "Passport series cannot be null"),
                Arguments.of("series", "123", "Passport series must be 4 digits"),
                Arguments.of("series", "12345", "Passport series must be 4 digits"),
                Arguments.of("series", "abcd", "Passport series must be 4 digits"),

                Arguments.of("number", null, "Passport number cannot be null"),
                Arguments.of("number", "12345", "Passport number must be 6 digits"),
                Arguments.of("number", "1234567", "Passport number must be 6 digits"),
                Arguments.of("number", "abcdef", "Passport number must be 6 digits")
        );
    }

    private void assertSingleViolation(LoanStatementRequestDto dto, String expectedMessage) {

        Set<ConstraintViolation<LoanStatementRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals(expectedMessage, violations.iterator().next().getMessage());
    }
}