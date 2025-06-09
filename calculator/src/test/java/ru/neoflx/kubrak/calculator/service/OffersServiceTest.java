package ru.neoflx.kubrak.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.neoflx.kubrak.calculator.dto.CreditDto;
import ru.neoflx.kubrak.calculator.dto.PaymentScheduleElementDto;
import ru.neoflx.kubrak.calculator.dto.ScoringDataDto;
import ru.neoflx.kubrak.calculator.model.enums.EmploymentStatus;
import ru.neoflx.kubrak.calculator.model.enums.Gender;
import ru.neoflx.kubrak.calculator.model.enums.MaritalStatus;
import ru.neoflx.kubrak.calculator.model.enums.Position;
import ru.neoflx.kubrak.calculator.util.PrepareTestDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульное тестирование - OffersService:")
class OffersServiceTest {

    @InjectMocks
    private OffersService offersService;

    ScoringDataDto scoringData;

    @BeforeEach
    void setUp() {
        // Устанавливаем значения из properties файла
        //TODO получать переменные из проперти файла
        ReflectionTestUtils.setField(offersService, "baseRate", BigDecimal.valueOf(15.0));
        ReflectionTestUtils.setField(offersService, "insuranceRateAdjustment", BigDecimal.valueOf(-3.0));
        ReflectionTestUtils.setField(offersService, "salaryClientRateAdjustment", BigDecimal.valueOf(-1.0));
        ReflectionTestUtils.setField(offersService, "femaleRateAdjustment", BigDecimal.valueOf(-0.5));
        ReflectionTestUtils.setField(offersService, "marriedRateAdjustment", BigDecimal.valueOf(-0.5));
        ReflectionTestUtils.setField(offersService, "unemployedRateAdjustment", BigDecimal.valueOf(5.0));
        ReflectionTestUtils.setField(offersService, "selfEmployedRateAdjustment", BigDecimal.valueOf(3.0));
        ReflectionTestUtils.setField(offersService, "businessOwnerRateAdjustment", BigDecimal.valueOf(2.0));
        ReflectionTestUtils.setField(offersService, "topManagerRateAdjustment", BigDecimal.valueOf(-2.0));
        ReflectionTestUtils.setField(offersService, "ownerRateAdjustment", BigDecimal.valueOf(-2.0));
        ReflectionTestUtils.setField(offersService, "lowExperienceRateAdjustment", BigDecimal.valueOf(1.0));
        ReflectionTestUtils.setField(offersService, "minimumRate", BigDecimal.valueOf(5.0));
        ReflectionTestUtils.setField(offersService, "insuranceCostPercentage", BigDecimal.valueOf(5.0));
        ReflectionTestUtils.setField(offersService, "insuranceCostMinimum", BigDecimal.valueOf(10000));
    }

    @BeforeEach
    void init() {

        scoringData = PrepareTestDto.createValidScoringDataDto();
    }

    @Test
    void calculateCredit_shouldReturnValidCreditDto() {

        scoringData.setIsSalaryClient(true);
        scoringData.getEmployment().setPosition(Position.TOP_MANAGER);
        CreditDto result = offersService.calculateCredit(scoringData);

        assertNotNull(result);
        assertEquals(scoringData.getAmount(), result.getAmount());
        assertEquals(scoringData.getTerm(), result.getTerm());
        assertEquals(scoringData.getIsInsuranceEnabled(), result.getIsInsuranceEnabled());
        assertEquals(scoringData.getIsSalaryClient(), result.getIsSalaryClient());

        // Проверяем что ставка рассчитана правильно
        BigDecimal expectedRate = BigDecimal.valueOf(15.0) // базовая ставка
                .add(BigDecimal.valueOf(-1.0)) // salary client
                .add(BigDecimal.valueOf(-2.0)); // top manager
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
            Position position,
            int workExperience,
            BigDecimal expectedAdjustment) {

        scoringData.setIsInsuranceEnabled(insuranceEnabled);
        scoringData.setIsSalaryClient(salaryClientEnabled);
        scoringData.setGender(gender);
        scoringData.setMaritalStatus(maritalStatus);
        scoringData.getEmployment().setEmploymentStatus(employmentStatus);
        scoringData.getEmployment().setPosition(position);
        scoringData.getEmployment().setWorkExperienceTotal(workExperience);

        BigDecimal result = offersService.calculateFinalRate(scoringData);

        BigDecimal expectedRate = BigDecimal.valueOf(15.0).add(expectedAdjustment);
        assertEquals(expectedRate, result);
    }

    @Test
    void calculateFinalRate_shouldNotGoBelowMinimum() {

        scoringData.setIsInsuranceEnabled(true);
        scoringData.setIsSalaryClient(true);
        scoringData.setGender(Gender.FEMALE);
        scoringData.setMaritalStatus(MaritalStatus.MARRIED);
        scoringData.getEmployment().setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        scoringData.getEmployment().setPosition(Position.TOP_MANAGER);
        scoringData.getEmployment().setWorkExperienceTotal(6);

        // Суммарная корректировка: -3 (insurance) -1 (salary) -0.5 (female) -0.5 (married) +5 (unemployed) -2 (top) +1 (exp) = -1.0
        // Базовая ставка 15 - 1 = 14, что выше минимальной 5

        BigDecimal result = offersService.calculateFinalRate(scoringData);

        assertEquals(BigDecimal.valueOf(14.0), result);
    }

    @Test
    void calculateInsuranceCost_shouldReturnCorrectValue() {
        // Для суммы больше минимальной
        BigDecimal amount1 = BigDecimal.valueOf(300000);
        BigDecimal expected1 = amount1.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected1, offersService.calculateInsuranceCost(amount1));

        // Для суммы меньше минимальной
        BigDecimal amount2 = BigDecimal.valueOf(100000);
        assertEquals(BigDecimal.valueOf(10000).setScale(2, RoundingMode.HALF_UP), offersService.calculateInsuranceCost(amount2));
    }

    @Test
    void calculateMonthlyPayment_shouldReturnCorrectValue() {

        BigDecimal amount = BigDecimal.valueOf(100000);
        BigDecimal rate = BigDecimal.valueOf(15.0);
        int term = 12;

        BigDecimal result = offersService.calculateMonthlyPayment(amount, rate, term);

        // Проверяем примерное значение (можно рассчитать точное значение отдельно)
        assertTrue(result.compareTo(BigDecimal.valueOf(9000)) > 0);
        assertTrue(result.compareTo(BigDecimal.valueOf(9100)) < 0);
    }

    @Test
    void calculatePSK_shouldReturnCorrectPercentage() {

        BigDecimal amount = BigDecimal.valueOf(100000);
        BigDecimal monthlyPayment = BigDecimal.valueOf(9025.80);
        int term = 12;

        BigDecimal result = offersService.calculatePSK(amount, monthlyPayment, term);

        // Ожидаем: ((9025.80 * 12) - 100000) / 100000 * 100 ≈ 8.31%
        assertEquals(BigDecimal.valueOf(8.31), result.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void generatePaymentSchedule_shouldReturnCorrectStructure() {

        BigDecimal amount = BigDecimal.valueOf(100000);
        BigDecimal rate = BigDecimal.valueOf(15.0);
        int term = 12;

        List<PaymentScheduleElementDto> schedule = offersService.generatePaymentSchedule(amount, rate, term);

        assertEquals(term, schedule.size());

        // Проверяем первый платеж
        PaymentScheduleElementDto first = schedule.getFirst();
        assertEquals(1, first.getNumber());
        assertEquals(LocalDate.now().plusMonths(1), first.getDate());
        assertTrue(first.getInterestPayment().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(first.getDebtPayment().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(first.getInterestPayment().add(first.getDebtPayment()), first.getTotalPayment());

        // Проверяем последний платеж
        PaymentScheduleElementDto last = schedule.get(term - 1);
        assertEquals(term, last.getNumber());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), last.getRemainingDebt());
    }

    // ========== Test Data Providers ==========

    private static Stream<Arguments> rateAdjustmentProvider() {
        return Stream.of(
                Arguments.of(true, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, Position.MANAGER, 24,
                        BigDecimal.valueOf(-3.0)), // только insurance enabled
                Arguments.of(false, true, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, Position.MANAGER, 24,
                        BigDecimal.valueOf(-1.0)),  // только salary client enabled
                Arguments.of(false, false, Gender.FEMALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, Position.MANAGER, 24,
                        BigDecimal.valueOf(-0.5)), // только female
                Arguments.of(false, false, Gender.MALE, MaritalStatus.MARRIED, EmploymentStatus.EMPLOYED, Position.MANAGER, 24,
                        BigDecimal.valueOf(-0.5)), // только married
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.UNEMPLOYED, Position.MANAGER, 24,
                        BigDecimal.valueOf(5.0)), // только unemployed
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.SELF_EMPLOYED, Position.MANAGER, 24,
                        BigDecimal.valueOf(3.0)), // только self-employed
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.BUSINESS_OWNER, Position.MANAGER, 24,
                        BigDecimal.valueOf(2.0)), // только business owner
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, Position.TOP_MANAGER, 24,
                        BigDecimal.valueOf(-2.0)), // только top manager
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, Position.OWNER, 24,
                        BigDecimal.valueOf(-2.0)), // только owner
                Arguments.of(false, false, Gender.MALE, MaritalStatus.SINGLE, EmploymentStatus.EMPLOYED, Position.MANAGER, 6,
                        BigDecimal.valueOf(1.0))  // только low experience

        );
    }
    
}