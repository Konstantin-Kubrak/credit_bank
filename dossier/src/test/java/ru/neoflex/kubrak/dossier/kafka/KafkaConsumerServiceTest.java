package ru.neoflex.kubrak.dossier.kafka;

import org.junit.jupiter.api.Test;
import ru.neoflex.kubrak.dossier.dto.EmailMessageDto;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class KafkaConsumerServiceTest extends KafkaConsumerServiceBaseTest {

    @Test
    void handleFinishRegistration_ShouldProcessMessageCorrectly() {
        EmailMessageDto message = createTestMessage();
        
        kafkaConsumerService.handleFinishRegistration(message);
        
        verify(dossierService).sendEmail(eq(message), 
            eq("Для завершения регистрации перейдите по ссылке..."));
    }

    @Test
    void handleCreateDocuments_ShouldFormatMessageWithStatementId() {
        EmailMessageDto message = createTestMessage();
        
        kafkaConsumerService.handleCreateDocuments(message);
        
        verify(dossierService).sendEmail(eq(message), 
            eq(String.format("Ваши документы по заявке №%s готовы.", message.getStatementId())));
    }

    @Test
    void handleSendDocuments_ShouldFormatMessageWithStatementId() {
        EmailMessageDto message = createTestMessage();
        
        kafkaConsumerService.handleSendDocuments(message);
        
        verify(dossierService).sendEmail(eq(message), 
            eq(String.format("Пожалуйста, подпишите документы по заявке №%s", message.getStatementId())));
    }

    @Test
    void handleSendSes_ShouldProcessMessageCorrectly() {
        EmailMessageDto message = createTestMessage();
        
        kafkaConsumerService.handleSendSes(message);
        
        verify(dossierService).sendEmail(eq(message), 
            eq("Для подписания документов перейдите по ссылке..."));
    }

    @Test
    void handleCreditIssued_ShouldFormatMessageWithStatementId() {
        EmailMessageDto message = createTestMessage();
        
        kafkaConsumerService.handleCreditIssued(message);
        
        verify(dossierService).sendEmail(eq(message), 
            eq(String.format("Поздравляем! Ваш кредит по заявке №%s оформлен.", message.getStatementId())));
    }

    @Test
    void handleStatementDenied_ShouldFormatMessageWithStatementId() {
        EmailMessageDto message = createTestMessage();
        
        kafkaConsumerService.handleStatementDenied(message);
        
        verify(dossierService).sendEmail(eq(message), 
            eq(String.format("К сожалению, ваша заявка №%s была отклонена.", message.getStatementId())));
    }
}