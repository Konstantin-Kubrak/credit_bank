package ru.neoflex.kubrak.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.neoflex.kubrak.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.kubrak.deal.service.DealService;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/deal")
public class CreditController {

    private final DealService dealService;

    @Operation(summary = "Calculate credit terms", responses = {
            @ApiResponse(responseCode = "201", description = "Credit calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Statement not found"),
            @ApiResponse(responseCode = "500", description = "Credit calculation failed")
    })
    @Parameter(description = "Statement id", example = "123e4567-e89b-12d3-a456-426614174000")
    @PostMapping("/calculate/{statementId}")
    public ResponseEntity<?> calculate(@PathVariable("statementId") UUID statementId,
                                       @Valid @RequestBody FinishRegistrationRequestDto frrDto) {

        log.info("Received credit creation request for statementId: {}", statementId);
        dealService.finishCreditRegistration(statementId, frrDto);
        log.info("Credit creation completed for statementId: {}", statementId);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
}