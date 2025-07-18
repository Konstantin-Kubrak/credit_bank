package ru.neoflex.kubrak.statement.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.neoflex.kubrak.statement.dto.LoanOfferDto;
import ru.neoflex.kubrak.statement.dto.LoanStatementRequestDto;
import ru.neoflex.kubrak.statement.service.StatementService;

import java.util.List;

@Slf4j
@RestController
public class StatementController {


    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @Operation(summary = "Pre-score loan statement and get offers", responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of loan offers provided successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanOfferDto.class))),
            @ApiResponse(responseCode = "503", description = "Calculator service unavailable")
    })
    @PostMapping(value = "/statement", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<LoanOfferDto> statement(@RequestBody LoanStatementRequestDto requestDto) {

        log.info("Received new loan statement request for client: {}", requestDto.getEmail());
        List<LoanOfferDto> loanOfferDtoList = statementService.getLoanOffers(requestDto);
        log.info("Successfully generated {} loan offers for client: {}", loanOfferDtoList.size(), requestDto.getEmail());

        return loanOfferDtoList;
    }

    @Operation(summary = "Select loan offer",
            description = "Submit selected loan offer to the system")
    @ApiResponse(responseCode = "204", description = "Offer successfully selected")
    @PostMapping("/statement/offer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void selectOffer(@RequestBody LoanOfferDto offerDto) {

        log.info("Received selected offer: {}", offerDto.getStatementId());
        statementService.selectOffer(offerDto);
    }
}