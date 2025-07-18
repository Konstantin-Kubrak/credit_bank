package ru.neoflex.kubrak.statement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.neoflex.kubrak.statement.dto.LoanOfferDto;
import ru.neoflex.kubrak.statement.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.statement.exception.PreScoringException;
import ru.neoflex.kubrak.statement.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ValidationServiceTest {

    @Autowired
    private ValidationService validationService;

    private LoanStatementRequestDto validRequest;
    private LoanOfferDto validOffer;

    @BeforeEach
    void setUp() {
        validRequest = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(30000))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan@example.com")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("123456")
                .build();

        validOffer = new LoanOfferDto()
                .setStatementId(UUID.randomUUID())
                .setRequestedAmount(BigDecimal.valueOf(20000))
                .setTotalAmount(BigDecimal.valueOf(25000))
                .setTerm(12)
                .setMonthlyPayment(BigDecimal.valueOf(2000))
                .setRate(BigDecimal.valueOf(10))
                .setIsInsuranceEnabled(true)
                .setIsSalaryClient(false);
    }

    @Test
    void preScoring_ValidRequest_NoExceptionThrown() {
        assertDoesNotThrow(() -> validationService.preScoring(validRequest));
    }

    @Test
    void preScoring_NullFirstName_ThrowsValidationException() {
        validRequest.setFirstName(null);
        assertThrows(ValidationException.class, () -> validationService.preScoring(validRequest));
    }

    @Test
    void preScoring_InvalidEmail_ThrowsPreScoringException() {
        validRequest.setEmail("invalid-email");
        assertThrows(PreScoringException.class, () -> validationService.preScoring(validRequest));
    }

    @Test
    void preScoring_AmountBelowMinimum_ThrowsPreScoringException() {
        validRequest.setAmount(BigDecimal.valueOf(10000));
        assertThrows(PreScoringException.class, () -> validationService.preScoring(validRequest));
    }

    @Test
    void validateLoanOffer_ValidOffer_NoExceptionThrown() {
        assertDoesNotThrow(() -> validationService.validateLoanOffer(validOffer));
    }

    @Test
    void validateLoanOffer_NullStatementId_ThrowsValidationException() {
        validOffer.setStatementId(null);
        assertThrows(ValidationException.class, () -> validationService.validateLoanOffer(validOffer));
    }

    @Test
    void validateLoanOffer_NegativeRate_ThrowsIllegalArgumentException() {
        validOffer.setRate(BigDecimal.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateLoanOffer(validOffer));
    }

    @Test
    void validateAmount_NullAmount_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> validationService.validateAmount(null, 1000));
    }

    @Test
    void validateTerm_InvalidTerm_ThrowsPreScoringException() {
        assertThrows(PreScoringException.class, () -> validationService.validateTerm(5, 6, 60));
    }

    @Test
    void validateName_TooShortName_ThrowsPreScoringException() {
        assertThrows(PreScoringException.class,
                () -> validationService.validateName("A", "Test", 2, 30));
    }

    @Test
    void validateEmail_InvalidFormat_ThrowsPreScoringException() {
        assertThrows(PreScoringException.class,
                () -> validationService.validateEmail("invalid-email"));
    }

    @Test
    void validateBirthdate_Underage_ThrowsPreScoringException() {
        LocalDate underageBirthdate = LocalDate.now().minusYears(17);
        assertThrows(PreScoringException.class,
                () -> validationService.validateBirthdate(underageBirthdate, 18));
    }

    @Test
    void validatePassportSeries_InvalidFormat_ThrowsPreScoringException() {
        assertThrows(PreScoringException.class,
                () -> validationService.validatePassportSeries("12AB"));
    }

    @Test
    void validatePassportNumber_InvalidFormat_ThrowsPreScoringException() {
        assertThrows(PreScoringException.class,
                () -> validationService.validatePassportNumber("12345"));
    }

    @Test
    void validatePositiveDecimal_NegativeValue_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validatePositiveDecimal(BigDecimal.valueOf(-1), "Test"));
    }

    @Test
    void validatePositiveNumber_ZeroValue_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validatePositiveNumber(0, "Test"));
    }

    @Test
    void validateNotNull_NullValue_ThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> validationService.validateNotNull(null, "Test"));
    }

    @Test
    void validateNotBlank_EmptyString_ThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> validationService.validateNotBlank("   ", "Test"));
    }
}