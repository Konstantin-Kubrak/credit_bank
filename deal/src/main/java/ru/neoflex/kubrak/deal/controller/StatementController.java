package ru.neoflex.kubrak.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.deal.exception.CalculatorServiceException;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.service.OfferService;
import ru.neoflex.kubrak.deal.service.StatementService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/deal")
public class StatementController {

    private final StatementService statementService;
    private final OfferService offerService;

    @Operation(summary = "Create loan statement and get offers", responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of loan offers provided successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanOfferDto.class))),
            @ApiResponse(responseCode = "503", description = "Calculator service unavailable")
    })
    @PostMapping(value = "/statement", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> statement(@Valid @RequestBody LoanStatementRequestDto requestDto) {

        log.info("Received new loan statement request for client: {}",
                requestDto.getEmail());
        try {
            List<LoanOfferDto> loanOfferDtoList = offerService.getLoanOfferList(requestDto);
            log.info("Successfully generated {} loan offers for client: {}",
                    loanOfferDtoList.size(), requestDto.getEmail());

            return ResponseEntity.ok(loanOfferDtoList);
        } catch (CalculatorServiceException e) {

            log.error("Calculator service error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Calculator service error: " + e.getMessage());
        }
    }

    @Operation(summary = "Select a loan offer", responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Offer selected successfully"),
            @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    @PostMapping("/offer/select")
    public ResponseEntity<?> selectOffer(@Valid @RequestBody LoanOfferDto loanOfferDto) {

        log.info("Processing offer selection for statementId: {}.Offer details: rate={}, amount={}",
                loanOfferDto.getStatementId(), loanOfferDto.getRate(), loanOfferDto.getRequestedAmount());
        try {
            statementService.setStatementLoanOffer(loanOfferDto);
            log.info("Loan offer successfully selected for statementId: {}", loanOfferDto.getStatementId());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (StatementNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}