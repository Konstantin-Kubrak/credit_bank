package ru.neoflex.kubrak.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.neoflex.kubrak.gateway.client.DealClient;
import ru.neoflex.kubrak.gateway.dto.FinishRegistrationRequestDto;
import ru.neoflex.kubrak.gateway.model.entity.Statement;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deals")
@Tag(name = "Deal Gateway", description = "API for deal processing")
public class DealController {

    private final DealClient dealClient;

    @Operation(summary = "Finish registration")
    @ApiResponse(responseCode = "201", description = "Registration completed successfully")
    @PostMapping("/{dealId}/registration")
    public ResponseEntity<Void> finishRegistration(
            @PathVariable UUID dealId,
            @RequestBody FinishRegistrationRequestDto request) {

        log.info("Finishing registration for deal: {}", dealId);
        dealClient.finishCreditRegistration(dealId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Send documents")
    @ApiResponse(responseCode = "201", description = "Documents sending initiated")
    @PostMapping("/{dealId}/documents/send")
    public ResponseEntity<Void> sendDocuments(@PathVariable UUID dealId) {

        log.info("Sending documents for deal: {}", dealId);
        dealClient.createDocuments(dealId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Sign documents")
    @ApiResponse(responseCode = "200", description = "Documents signing initiated")
    @PostMapping("/{dealId}/documents/sign")
    public ResponseEntity<Void> signDocuments(@PathVariable UUID dealId) {

        log.info("Signing documents for deal: {}", dealId);
        dealClient.signDocuments(dealId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Verify code")
    @ApiResponse(responseCode = "200", description = "Documents signing completed")
    @PostMapping("/{dealId}/documents/code")
    public ResponseEntity<Void> verifyCode(@PathVariable UUID dealId) {

        log.info("Verifying code for deal: {}", dealId);
        dealClient.verifyCode(dealId);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get statement by ID",
            description = "Retrieves a specific deal statement by its unique identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statement found"),
                    @ApiResponse(responseCode = "404", description = "Statement not found")
            }
    )
    @GetMapping("/statements/{statementId}")
    public ResponseEntity<Statement> getStatement(@PathVariable UUID statementId) {

        log.info("Admin request for statement: {}", statementId);
        Statement statement = dealClient.getStatementById(statementId);

        return ResponseEntity.ok(statement);
    }

    @Operation(
            summary = "Get all statements",
            description = "Retrieves all available deal statements",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of statements retrieved")
            }
    )
    @GetMapping("/statements")
    public ResponseEntity<List<Statement>> getAllStatements() {

        log.info("Admin request for all statements");
        List<Statement> statements = dealClient.getAllStatements();

        return ResponseEntity.ok(statements);
    }
}
