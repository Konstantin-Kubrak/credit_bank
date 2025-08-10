package ru.neoflex.kubrak.deal.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.service.StatementService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatementService statementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getStatementById_ShouldReturnStatement() throws Exception {

        Statement statement = new Statement();
        UUID uuid = UUID.randomUUID();
        statement.setStatementId(uuid);

        given(statementService.getStatement(uuid)).willReturn(statement);

        mockMvc.perform(get("/deal/admin/statement/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementId").value(uuid.toString()));
    }

    @Test
    void getAllStatements_ShouldReturnAllStatements() throws Exception {

        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Statement statement1 = new Statement();
        statement1.setStatementId(uuid1);
        Statement statement2 = new Statement();
        statement2.setStatementId(uuid2);
        List<Statement> statements = Arrays.asList(statement1, statement2);

        given(statementService.getAllStatement()).willReturn(statements);

        mockMvc.perform(get("/deal/admin/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].statementId").value(uuid1.toString()))
                .andExpect(jsonPath("$[1].statementId").value(uuid2.toString()));
    }

    @Test
    void getStatementById_ShouldReturnNotFound_WhenStatementNotExists() throws Exception {

        UUID notFoundUuid = UUID.randomUUID();
        given(statementService.getStatement(notFoundUuid))
                .willThrow(new StatementNotFoundException(notFoundUuid));

        mockMvc.perform(get("/deal/admin/statement/"+notFoundUuid))
                .andExpect(status().isNotFound());
    }
}