package ru.neoflx.kubrak.calculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.neoflx.kubrak.calculator.dto.CreditDto;
import ru.neoflx.kubrak.calculator.dto.LoanOfferDto;
import ru.neoflx.kubrak.calculator.dto.LoanStatementRequestDto;
import ru.neoflx.kubrak.calculator.dto.ScoringDataDto;
import ru.neoflx.kubrak.calculator.service.CalcService;
import ru.neoflx.kubrak.calculator.service.OffersService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
@Tag(name = "Credit Calculator", description = "API for credit calculations and offers")
public class CalculatorController {

    private final CalcService calculatorService;
    private final OffersService offersService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/offers")
    @Operation(summary = "Get credit offers",
            description = "Calculate possible credit offers based on request data")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoanOfferDto.class)))
    public List<LoanOfferDto> offers(@Valid @RequestBody LoanStatementRequestDto lsrDto){

        log.info("Received loan statement request: {}", lsrDto);
        List<LoanOfferDto> listOfLoanOfferDto = calculatorService.calculateLoanOffers(lsrDto);
        log.info("Calculated loan offers: {}", listOfLoanOfferDto);

        return listOfLoanOfferDto;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/calc")
    @Operation(summary = "Calculate full credit parameters",
            description = "Validate and score data, then calculate full credit parameters")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CreditDto.class)))
    public CreditDto calc(@Valid @RequestBody ScoringDataDto scoringDataDto){

        log.info("Received scoring data: {}", scoringDataDto);
        CreditDto creditDto = offersService.calculateCredit(scoringDataDto);
        log.info("Calculated credit details: {}", creditDto);

        return creditDto;
    }
}
