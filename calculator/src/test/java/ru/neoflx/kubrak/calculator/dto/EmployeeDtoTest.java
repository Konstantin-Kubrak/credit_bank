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
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("Модульное тестирование - валидация EmployeeDto:")
class EmployeeDtoTest {

    @Autowired
    private LocalValidatorFactoryBean validator;
    
    EmploymentDto dto;
    
    @BeforeEach
    void setUp() {
        dto = PrepareTestDto.createValidEmploymentDto();
    }

    @Test
    void whenEmploymentValid_thenNoViolations() {

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenEmploymentStatusNull_thenValidationFails() {

        dto.setEmploymentStatus(null);
        assertSingleViolation(dto, "Employment status cannot be null");
    }

    @ParameterizedTest
    @MethodSource("invalidInnProvider")
    void whenEmployerInnInvalid_thenValidationFails(String inn, String expectedMessage) {

        dto.setEmployerINN(inn);
        assertSingleViolation(dto, expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("invalidSalaryProvider")
    void whenSalaryInvalid_thenValidationFails(BigDecimal salary, String expectedMessage) {

        dto.setSalary(salary);
        assertSingleViolation(dto, expectedMessage);
    }

    @Test
    void whenPositionNull_thenValidationFails() {

        dto.setPosition(null);
        assertSingleViolation(dto, "Job position cannot be null");
    }

    @ParameterizedTest
    @MethodSource("invalidExperienceProvider")
    void whenExperienceInvalid_thenValidationFails(String fieldName, Integer value, String expectedMessage) {

        switch (fieldName) {
            case "total" -> dto.setWorkExperienceTotal(value);
            case "current" -> dto.setWorkExperienceCurrent(value);
        }
        assertSingleViolation(dto, expectedMessage);
    }

    // ========== Test Data Providers ==========

    private static Stream<Arguments> invalidInnProvider() {
        return Stream.of(
                Arguments.of(null, "Employer INN cannot be null"),
                Arguments.of("", "INN must be 10 or 12 digits"),
                Arguments.of("123456789", "INN must be 10 or 12 digits"),
                Arguments.of("abcdefghij", "INN must be 10 or 12 digits")
        );
    }

    private static Stream<Arguments> invalidSalaryProvider() {
        return Stream.of(
                Arguments.of(null, "Salary cannot be null"),
                Arguments.of(BigDecimal.ZERO, "Salary must be positive"),
                Arguments.of(BigDecimal.valueOf(-100), "Salary must be positive"),
                Arguments.of(new BigDecimal("12345678901.123"), "Salary must have up to 10 integer and 2 fraction digits")
        );
    }

    private static Stream<Arguments> invalidExperienceProvider() {
        return Stream.of(
                Arguments.of("total", null, "Total work experience cannot be null"),
                Arguments.of("total", -1, "Total work experience cannot be negative"),
                Arguments.of("total", 961, "Total work experience cannot be more than 960 months"),
                Arguments.of("current", null, "Current work experience cannot be null"),
                Arguments.of("current", -1, "Current work experience cannot be negative")
        );
    }

    
    private <T> void assertSingleViolation(T dto, String expectedMessage) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        assertEquals(1, violations.size(), "Expected exactly one violation");
        assertEquals(expectedMessage, violations.iterator().next().getMessage());
    }
}