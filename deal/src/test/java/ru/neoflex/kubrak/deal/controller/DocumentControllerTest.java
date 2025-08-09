package ru.neoflex.kubrak.deal.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.neoflex.kubrak.deal.service.DealService;
import ru.neoflex.kubrak.deal.service.DossierService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DealService dealService;

    @Mock
    private DossierService dossierService;

    @InjectMocks
    private DocumentController documentController;

    @Test
    void documentsSend_ShouldReturnCreatedStatus() {
        UUID statementId = UUID.randomUUID();
        ResponseEntity<?> response = documentController.documentsSend(statementId);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(dealService, times(1)).createDocuments(statementId);
    }

    @Test
    void documentsSign_ShouldReturnOkStatus() {
        UUID statementId = UUID.randomUUID();
        ResponseEntity<?> response = documentController.documentsSign(statementId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dossierService, times(1)).sendKafkaSendSes(statementId);
    }

    @Test
    void documentsCode_ShouldReturnOkStatus() {
        UUID statementId = UUID.randomUUID();
        ResponseEntity<?> response = documentController.documentsCode(statementId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dealService, times(1)).signDocuments(statementId);
    }
}