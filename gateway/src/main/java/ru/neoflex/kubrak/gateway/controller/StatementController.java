package ru.neoflex.kubrak.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.neoflex.kubrak.gateway.client.StatementClient;
import ru.neoflex.kubrak.gateway.dto.LoanOfferDto;
import ru.neoflex.kubrak.gateway.dto.LoanStatementRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statements")
@Tag(name = "Statement Gateway", description = "API for loan statements processing")
public class StatementController {

    private final StatementClient statementClient;

    @Operation(summary = "Submit loan application", responses = {
            @ApiResponse(responseCode = "200", description = "Application processed successfully",
                    content = @Content(schema = @Schema(implementation = LoanOfferDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @PostMapping
    public ResponseEntity<List<LoanOfferDto>> submitApplication(@RequestBody LoanStatementRequestDto request) {
        log.info("Received new loan application");
        List<LoanOfferDto> offers = statementClient.getLoanOffers(request);
        return ResponseEntity.ok(offers);
    }

    @Operation(summary = "Select loan offer", description = "Submit selected loan offer to the system")
    @ApiResponse(responseCode = "204", description = "Offer successfully selected")
    @PostMapping("/offers")
    public ResponseEntity<Void> selectOffer(@RequestBody LoanOfferDto offer) {
        log.info("Processing selected offer");
        statementClient.selectOffer(offer);
        return ResponseEntity.noContent().build();
    }
}
