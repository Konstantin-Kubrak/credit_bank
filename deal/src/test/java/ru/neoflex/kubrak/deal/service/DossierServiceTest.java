package ru.neoflex.kubrak.deal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.neoflex.kubrak.deal.dto.EmailMessageDto;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Client;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.repository.StatementRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DossierServiceTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private KafkaTemplate<String, EmailMessageDto> kafkaProducer;

    @InjectMocks
    private DossierService dossierService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dossierService, "topicFinishRegistration", "finish-registration");
        ReflectionTestUtils.setField(dossierService, "topicCreateDocuments", "create-documents");
        ReflectionTestUtils.setField(dossierService, "topicSendDocuments", "send-documents");
        ReflectionTestUtils.setField(dossierService, "topicSendSes", "send-ses");
        ReflectionTestUtils.setField(dossierService, "topicCreditIssued", "credit-issued");
        ReflectionTestUtils.setField(dossierService, "topicStatementDenied", "statement-denied");
    }
    @Test
    void sendKafkaFinishRegistration_ShouldSendMessage() {
        UUID statementId = UUID.randomUUID();
        Statement statement = createTestStatement(statementId);

        when(statementRepository.findByStatementId(statementId)).thenReturn(Optional.of(statement));

        dossierService.sendKafkaFinishRegistration(statementId);

        verify(statementRepository).findByStatementId(statementId);
        verify(kafkaProducer).send(anyString(), any(EmailMessageDto.class));
    }

    @Test
    void sendKafkaCreateDocuments_ShouldSendMessage() {
        UUID statementId = UUID.randomUUID();
        Statement statement = createTestStatement(statementId);

        when(statementRepository.findByStatementId(statementId)).thenReturn(Optional.of(statement));

        dossierService.sendKafkaCreateDocuments(statementId);

        verify(statementRepository).findByStatementId(statementId);
        verify(kafkaProducer).send(anyString(), any(EmailMessageDto.class));
    }

    @Test
    void sendKafkaSendDocuments_ShouldSendMessage() {
        UUID statementId = UUID.randomUUID();
        Statement statement = createTestStatement(statementId);

        when(statementRepository.findByStatementId(statementId)).thenReturn(Optional.of(statement));

        dossierService.sendKafkaSendDocuments(statementId);

        verify(statementRepository).findByStatementId(statementId);
        verify(kafkaProducer).send(anyString(), any(EmailMessageDto.class));
    }

    @Test
    void sendKafkaSendSes_ShouldSendMessage() {
        UUID statementId = UUID.randomUUID();
        Statement statement = createTestStatement(statementId);

        when(statementRepository.findByStatementId(statementId)).thenReturn(Optional.of(statement));

        dossierService.sendKafkaSendSes(statementId);

        verify(statementRepository).findByStatementId(statementId);
        verify(kafkaProducer).send(anyString(), any(EmailMessageDto.class));
    }

    @Test
    void sendKafkaCreditIssued_ShouldSendMessage() {
        UUID statementId = UUID.randomUUID();
        Statement statement = createTestStatement(statementId);

        when(statementRepository.findByStatementId(statementId)).thenReturn(Optional.of(statement));

        dossierService.sendKafkaCreditIssued(statementId);

        verify(statementRepository).findByStatementId(statementId);
        verify(kafkaProducer).send(anyString(), any(EmailMessageDto.class));
    }

    @Test
    void sendKafkaStatementDenied_ShouldSendMessage() {
        UUID statementId = UUID.randomUUID();
        Statement statement = createTestStatement(statementId);

        when(statementRepository.findByStatementId(statementId)).thenReturn(Optional.of(statement));

        dossierService.sendKafkaStatementDenied(statementId);

        verify(statementRepository).findByStatementId(statementId);
        verify(kafkaProducer).send(anyString(), any(EmailMessageDto.class));
    }

    @Test
    void sendKafkaMethods_ShouldThrowExceptionWhenStatementNotFound() {
        UUID statementId = UUID.randomUUID();

        when(statementRepository.findByStatementId(statementId)).thenReturn(Optional.empty());

        assertThrows(StatementNotFoundException.class, () -> dossierService.sendKafkaFinishRegistration(statementId));
        assertThrows(StatementNotFoundException.class, () -> dossierService.sendKafkaCreateDocuments(statementId));
        assertThrows(StatementNotFoundException.class, () -> dossierService.sendKafkaSendDocuments(statementId));
        assertThrows(StatementNotFoundException.class, () -> dossierService.sendKafkaSendSes(statementId));
        assertThrows(StatementNotFoundException.class, () -> dossierService.sendKafkaCreditIssued(statementId));
        assertThrows(StatementNotFoundException.class, () -> dossierService.sendKafkaStatementDenied(statementId));
    }

    private Statement createTestStatement(UUID statementId) {
        Client client = new Client();
        client.setEmail("test@example.com");

        Statement statement = new Statement();
        statement.setStatementId(statementId);
        statement.setClient(client);

        return statement;
    }
}