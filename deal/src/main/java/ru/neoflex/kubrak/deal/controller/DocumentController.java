package ru.neoflex.kubrak.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.neoflex.kubrak.deal.service.DealService;
import ru.neoflex.kubrak.deal.service.DossierService;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Document Controller", description = "Controller for managing document operations in the deal process")
@RequestMapping("/deal/")
public class DocumentController {

    private final DealService dealService;
    private final DossierService dossierService;

    @Operation(summary = "Send documents", description = "Trigger the process of sending documents for a specific statement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documents sending process initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Statement not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/document/{statementId}/send")
    public ResponseEntity<?> documentsSend(
            @Parameter(description = "UUID of the statement", required = true)
            @PathVariable("statementId") UUID statementId) {

        log.info("Received request to send documents for statement ID: {}", statementId);
        dealService.createDocuments(statementId);
        log.info("Successfully initiated document sending for statement ID: {}", statementId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Sign documents", description = "Trigger the process of signing documents for a specific statement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents signing process initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Statement not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/document/{statementId}/sign")
    public ResponseEntity<?> documentsSign(
            @Parameter(description = "UUID of the statement", required = true)
            @PathVariable("statementId") UUID statementId) {

        log.info("Received request to sign documents for statement ID: {}", statementId);
        dossierService.sendKafkaSendSes(statementId);
        log.info("Successfully initiated document signing for statement ID: {}", statementId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Verify code and complete signing", description = "Complete the document signing process after code verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document signing completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Statement not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/document/{statementId}/code")
    public ResponseEntity<?> documentsCode(
            @Parameter(description = "UUID of the statement", required = true)
            @PathVariable("statementId") UUID statementId) {

        log.info("Received request to verify code and complete signing for statement ID: {}", statementId);
        dealService.signDocuments(statementId);
        log.info("Successfully completed document signing process for statement ID: {}", statementId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}