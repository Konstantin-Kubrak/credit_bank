package ru.neoflex.kubrak.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.service.StatementService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/deal")
@Tag(name = "Statement Administration", description = "API for managing deal statements")
public class AdminController {

    private final StatementService statementService;

    @GetMapping("/admin/statement/{statementId}")
    @Operation(
            summary = "Get statement by ID",
            description = "Retrieves a specific deal statement by its unique identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statement found"),
                    @ApiResponse(responseCode = "404", description = "Statement not found")
            }
    )
    public Statement getStatementById(@PathVariable UUID statementId) {

        log.info("Request received to get statement with ID: {}", statementId);
        Statement statement = statementService.getStatement(statementId);
        log.info("Successfully retrieved statement: {}", statementId);

        return statement;
    }

    @GetMapping("/admin/statement")
    @Operation(
            summary = "Get all statements",
            description = "Retrieves all available deal statements",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of statements retrieved")
            }
    )
    public List<Statement> getAllStatements() {

        log.info("Request received to get all statements");
        List<Statement> statements = statementService.getAllStatement();
        log.info("Successfully retrieved {} statements", statements.size());

        return statements;
    }
}
