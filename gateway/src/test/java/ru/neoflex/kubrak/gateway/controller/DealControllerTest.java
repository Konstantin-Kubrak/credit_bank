package ru.neoflex.kubrak.gateway.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.neoflex.kubrak.gateway.client.DealClient;
import ru.neoflex.kubrak.gateway.dto.FinishRegistrationRequestDto;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class DealControllerTest {

    @Mock
    private DealClient dealClient;

    @InjectMocks
    private DealController dealController;

    @Test
    void finishRegistration_ShouldReturnCreated() {

        UUID dealId = UUID.randomUUID();
        FinishRegistrationRequestDto request = FinishRegistrationRequestDto.builder().build();

        ResponseEntity<Void> response = dealController.finishRegistration(dealId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(dealClient, times(1)).finishCreditRegistration(dealId, request);
    }

    @Test
    void sendDocuments_ShouldReturnCreated() {

        UUID dealId = UUID.randomUUID();

        ResponseEntity<Void> response = dealController.sendDocuments(dealId);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(dealClient, times(1)).createDocuments(dealId);
    }

    @Test
    void signDocuments_ShouldReturnOk() {

        UUID dealId = UUID.randomUUID();

        ResponseEntity<Void> response = dealController.signDocuments(dealId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dealClient, times(1)).signDocuments(dealId);
    }

    @Test
    void verifyCode_ShouldReturnOk() {

        UUID dealId = UUID.randomUUID();

        ResponseEntity<Void> response = dealController.verifyCode(dealId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dealClient, times(1)).verifyCode(dealId);
    }
}