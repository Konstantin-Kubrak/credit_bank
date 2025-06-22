package ru.neoflex.kubrak.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.neoflex.kubrak.calculator.dto.CreditDto;
import ru.neoflex.kubrak.calculator.dto.PaymentScheduleElementDto;
import ru.neoflex.kubrak.calculator.dto.ScoringDataDto;
import ru.neoflex.kubrak.calculator.exception.InvalidCreditAmountException;
import ru.neoflex.kubrak.calculator.exception.InvalidEmploymentStatusOrPositionException;
import ru.neoflex.kubrak.calculator.model.enums.EmploymentPosition;
import ru.neoflex.kubrak.calculator.model.enums.EmploymentStatus;
import ru.neoflex.kubrak.calculator.model.enums.Gender;
import ru.neoflex.kubrak.calculator.model.enums.MaritalStatus;
import ru.neoflex.kubrak.calculator.util.PrepareTestDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
@DisplayName("Module testing - OffersService:")
class OffersServiceTest {

    @MockitoBean
    private BaseCalculationsService baseCalc;
    @Autowired
    private OffersService offersService;
    private ScoringDataDto scoringData;

    @BeforeEach
    void setUp() {

        when(baseCalc.calculateInsuranceCost(any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(5000));
        when(baseCalc.calculateMonthlyPayment(any(BigDecimal.class), any(BigDecimal.class), any(Integer.class)))
                .thenReturn(BigDecimal.valueOf(9000));
        scoringData = PrepareTestDto.createValidScoringDataDto();
    }

    @Test
    @DisplayName("calculateCredit_should return valid creditDto")
    void calculateCredit_shouldReturnValidCreditDto() throws
            InvalidEmploymentStatusOrPositionException, InvalidCreditAmountException {

        scoringData.setIsSalaryClient(true);
        scoringData.getEmployment().setEmploymentPosition(EmploymentPosition.TOP_MANAGER);
        CreditDto result = offersService.calculateCredit(scoringData);

        assertNotNull(result);
        assertEquals(scoringData.getAmount(), result.getAmount());
        assertEquals(scoringData.getTerm(), result.getTerm());
        assertEquals(scoringData.getIsInsuranceEnabled(), result.getIsInsuranceEnabled());
        assertEquals(scoringData.getIsSalaryClient(), result.getIsSalaryClient());

        BigDecimal expectedRate = BigDecimal.valueOf(15.0)
                .add(BigDecimal.valueOf(-1.0))
                .add(BigDecimal.valueOf(-2.0));
        assertEquals(expectedRate, result.getRate());

        assertNotNull(result.getMonthlyPayment());
        assertNotNull(result.getPsk());
        assertNotNull(result.getPaymentSchedule());
        assertEquals(scoringData.getTerm(), result.getPaymentSchedule().size());
    }

    @ParameterizedTest
    @MethodSource("rateAdjustmentProvider")
    void calculateFinalRate_shouldApplyCorrectAdjustments(
            boolean insuranceEnabled,
            boolean salaryClientEnabled,
            Gender gender,
            MaritalStatus maritalStatus,
            EmploymentStatus employmentStatus,
            EmploymentPosition employmentPosition,
            int workExperience,
            BigDecimal expectedAdjustment) {

        scoringData.setIsInsuranceEnabled(insuranceEnabled);
        scoringData.setIsSalaryClient(salaryClientEnabled);
        scoringData.setGender(gender);
        scoringData.setMaritalStatus(maritalStatus);
        scoringData.getEmployment().setEmploymentStatus(employmentStatus);
        scoringData.getEmployment().setEmploymentPosition(employmentPosition);
        scoringData.getEmployment().setWorkExperienceTotal(workExperience);

        BigDecimal result = offersService.calculateFinalRate(scoringData);

        BigDecimal expectedRate = BigDecimal.valueOf(15.0).add(expectedAdjustment);
        assertEquals(expectedRate, result);
    }


    @Test
    void calculateFinalRate_shouldNotGoBelowMinimum() {

        scoringData.setIsInsuranceEnabled(true)
                .setIsSalaryClient(true)
                .setGender(Gender.FEMALE)
                .setMaritalStatus(MaritalStatus.MARRIED);
        scoringData.getEmployment()
                .setEmploymentStatus(EmploymentStatus.EMPLOYED)
                .setEmploymentPosition(EmploymentPosition.TOP_MANAGER)
                .setWorkExperienceTotal(24);

        BigDecimal result = offersService.calculateFinalRate(scoringData);

        assertTrue(result.compareTo(BigDecimal.valueOf(5.0)) >= 0);
    }

    @Test
    void calculatePSK_shouldReturnCorrectPercentage() {

        BigDecimal amount = BigDecimal.valueOf(100000);
        BigDecimal monthlyPayment = BigDecimal.valueOf(9025.80);
        int term = 12;

        BigDecimal result = offersService.calculatePSK(amount, monthlyPayment, term);

        assertEquals(BigDecimal.valueOf(8.31), result.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("generatePaymentSchedule_should return correct structure")
    void generatePaymentSchedule_shouldReturnCorrectStructure() {

        BigDecimal amount = BigDecimal.valueOf(100000);
        BigDecimal rate = BigDecimal.valueOf(15.0);
        int term = 12;

        when(baseCalc.calculateMonthlyPayment(amount, rate, term))
                .thenReturn(BigDecimal.valueOf(9025.80));

        List<PaymentScheduleElementDto> schedule = offersService.generatePaymentSchedule(amount, rate, term);

        assertEquals(term, schedule.size());

        PaymentScheduleElementDto first = schedule.getFirst();
        assertEquals(1, first.getNumber());
        assertTrue(first.getInterestPayment().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(first.getDebtPayment().compareTo(BigDecimal.ZERO) > 0);

        PaymentScheduleElementDto last = schedule.get(term - 1);
        assertEquals(term, last.getNumber());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), last.getRemainingDebt());
    }

    @Test
    @DisplayName("scoringDataValidation_should throw for business owner with non-owner position")
    void scoringDataValidation_shouldThrowForBusinessOwnerWithNonOwnerPosition() {
        scoringData.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);
        scoringData.getEmployment().setEmploymentPosition(EmploymentPosition.MANAGER); // Не OWNER

        assertThrows(InvalidEmploymentStatusOrPositionException.class,
                () -> offersService.scoringDataValidation(scoringData));
    }

    @Test
    @DisplayName("scoringDataValidation_pass business owner with owner position")
    void scoringDataValidation_shouldPassForBusinessOwnerWithOwnerPosition() {
        scoringData.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);
        scoringData.getEmployment().setEmploymentPosition(EmploymentPosition.OWNER);

        assertDoesNotThrow(() -> offersService.scoringDataValidation(scoringData));
    }

    @Test
    @DisplayName("scoringDataValidation_exception self-employed with top manager or worker position")
    void scoringDataValidation_shouldThrowForSelfEmployedWithInvalidPositions() {
        scoringData.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);

        scoringData.getEmployment().setEmploymentPosition(EmploymentPosition.TOP_MANAGER);
        assertThrows(InvalidEmploymentStatusOrPositionException.class,
                () -> offersService.scoringDataValidation(scoringData));

        scoringData.getEmployment().setEmploymentPosition(EmploymentPosition.WORKER);
        assertThrows(InvalidEmploymentStatusOrPositionException.class,
                () -> offersService.scoringDataValidation(scoringData));
    }

    @Test
    void scoringDataValidation_shouldThrowForUnemployed() {
        scoringData.getEmployment()
                .setEmploymentStatus(EmploymentStatus.UNEMPLOYED)
                .setEmploymentPosition(EmploymentPosition.UNEMPLOYED);

        assertThrows(InvalidEmploymentStatusOrPositionException.class,
                () -> offersService.scoringDataValidation(scoringData));
    }

    @Test
    void scoringDataValidation_shouldThrowWhenAmountExceeds24Salaries() {
        scoringData.setAmount(BigDecimal.valueOf(1000000));
        scoringData.getEmployment().setSalary(BigDecimal.valueOf(40000));

        assertThrows(InvalidCreditAmountException.class,
                () -> offersService.scoringDataValidation(scoringData));
    }

    @Test
    @DisplayName("scoringDataValidation_pass self-employed with valid positions")
    void scoringDataValidation_shouldPassForSelfEmployedWithValidPositions() {
        scoringData.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);

        scoringData.getEmployment().setEmploymentPosition(EmploymentPosition.MANAGER);
        assertDoesNotThrow(() -> offersService.scoringDataValidation(scoringData));

        scoringData.getEmployment().setEmploymentPosition(EmploymentPosition.OWNER);
        assertDoesNotThrow(() -> offersService.scoringDataValidation(scoringData));
    }

    @Test
    @DisplayName("scoringDataValidation_exception unemployed with non-unemployed position")
    void scoringDataValidation_shouldThrowForUnemployedWithWrongPosition() {

        scoringData.getEmployment().setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        scoringData.getEmployment().setEmploymentPosition(EmploymentPosition.MANAGER);

        assertThrows(InvalidEmploymentStatusOrPositionException.class,
                () -> offersService.scoringDataValidation(scoringData));
    }

    private static Stream<Arguments> rateAdjustmentProvider() {
        return Stream.of(
                Arguments.of(true, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, EmploymentPosition.MANAGER, 24,
                        BigDecimal.valueOf(-3.0)),
                Arguments.of(false, true, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, EmploymentPosition.MANAGER, 24,
                        BigDecimal.valueOf(-1.0)),
                Arguments.of(false, false, Gender.FEMALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, EmploymentPosition.MANAGER, 24,
                        BigDecimal.valueOf(-0.5)),
                Arguments.of(false, false, Gender.MALE, MaritalStatus.MARRIED, EmploymentStatus.EMPLOYED, EmploymentPosition.MANAGER, 24,
                        BigDecimal.valueOf(-0.5)),
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.SELF_EMPLOYED, EmploymentPosition.MANAGER, 24,
                        BigDecimal.valueOf(3.0)),
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.BUSINESS_OWNER, EmploymentPosition.MANAGER, 24,
                        BigDecimal.valueOf(2.0)),
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, EmploymentPosition.TOP_MANAGER, 24,
                        BigDecimal.valueOf(-2.0)),
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, EmploymentPosition.OWNER, 24,
                        BigDecimal.valueOf(-2.0)),
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, EmploymentPosition.MANAGER, 6,
                        BigDecimal.valueOf(1.0))

        );
    }
    
}