package ru.neoflex.kubrak.dossier.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.dossier.dto.EmailMessageDto;
import ru.neoflex.kubrak.dossier.service.DossierService;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final DossierService dossierService;

    @KafkaListener(topics = "${dossier.kafka.topic.finish-registration}")
    public void handleFinishRegistration(EmailMessageDto message) {

        log.info("[Kafka] Received message for finish-registration: {}",
                message);
        dossierService.sendEmail(message,
                "Для завершения регистрации перейдите по ссылке...");
        log.info("[Email] Sent finish-registration email to {}", message.getEmail());
    }

    @KafkaListener(topics = "${dossier.kafka.topic.create-documents}")
    public void handleCreateDocuments(EmailMessageDto message) {

        log.info("[Kafka] Received message for create-documents: {}",
                message);
        dossierService.sendEmail(message,
                String.format("Ваши документы по заявке №%s готовы.", message.getStatementId()));
        log.info("[Email] Sent create-documents email to {}", message.getEmail());
    }

    @KafkaListener(topics = "${dossier.kafka.topic.send-documents}")
    public void handleSendDocuments(EmailMessageDto message) {

        log.info("[Kafka] Received message for send-documents: {}",
                message);
        dossierService.sendEmail(message,
                String.format("Пожалуйста, подпишите документы по заявке №%s", message.getStatementId()));
        log.info("[Email] Sent send-documents email to {}", message.getEmail());
    }

    @KafkaListener(topics = "${dossier.kafka.topic.send-ses}")
    public void handleSendSes(EmailMessageDto message) {

        log.info("[Kafka] Received message for send-ses: {}", message);
        dossierService.sendEmail(message,
                "Для подписания документов перейдите по ссылке...");
        log.info("[Email] Sent send-ses email to {}", message.getEmail());
    }

    @KafkaListener(topics = "${dossier.kafka.topic.credit-issued}")
    public void handleCreditIssued(EmailMessageDto message) {

        log.info("[Kafka] Received message for credit-issued: {}",
                message);
        dossierService.sendEmail(message,
                String.format("Поздравляем! Ваш кредит по заявке №%s оформлен.", message.getStatementId()));
        log.info("[Email] Sent credit-issued email to {}", message.getEmail());
    }

    @KafkaListener(topics = "${dossier.kafka.topic.statement-denied}")
    public void handleStatementDenied(EmailMessageDto message) {

        log.info("[Kafka] Received message for statement-denied: {}",
                message);
        dossierService.sendEmail(message,
                String.format("К сожалению, ваша заявка №%s была отклонена.", message.getStatementId()));
        log.info("[Email] Sent statement-denied email to {}", message.getEmail());
    }
}