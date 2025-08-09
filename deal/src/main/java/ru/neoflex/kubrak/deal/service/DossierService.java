package ru.neoflex.kubrak.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.deal.dto.EmailMessageDto;
import ru.neoflex.kubrak.deal.exception.StatementNotFoundException;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.Theme;
import ru.neoflex.kubrak.deal.repository.StatementRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierService {

    @Value("${deal.kafka.topic.finish-registration}")
    private String topicFinishRegistration;
    @Value("${deal.kafka.topic.create-documents}")
    private String topicCreateDocuments;
    @Value("${deal.kafka.topic.send-documents}")
    private String topicSendDocuments;
    @Value("${deal.kafka.topic.send-ses}")
    private String topicSendSes;
    @Value("${deal.kafka.topic.credit-issued}")
    private String topicCreditIssued;
    @Value("${deal.kafka.topic.statement-denied}")
    private String topicStatementDenied;

    private final KafkaTemplate<String, EmailMessageDto>  kafkaProducer;
    private final StatementRepository statementRepository;

    public void sendKafkaFinishRegistration(UUID statementUUID) {

        log.info("Starting to send finish registration event for statement: {}", statementUUID);
        Statement statement = statementRepository.findByStatementId(statementUUID)
                .orElseThrow(() -> new StatementNotFoundException(statementUUID));
        log.debug("Retrieved statement for finish registration: {}", statement);

        EmailMessageDto emailMessage = EmailMessageDto.builder()
                .email(statement.getClient().getEmail())
                .statementId(statementUUID)
                .theme(Theme.FINISH_REGISTRATION)
                .text("some text")
                .build();
        log.debug("Constructed email message: {}", emailMessage);

        kafkaProducer.send(topicFinishRegistration, emailMessage);
        log.info("Successfully sent finish registration event for statement: {}", statementUUID);
    }

    public void sendKafkaCreateDocuments(UUID statementUUID) {

        log.info("Starting to send create documents event for statement: {}", statementUUID);
        Statement statement = statementRepository.findByStatementId(statementUUID)
                .orElseThrow(() -> new StatementNotFoundException(statementUUID));
        log.debug("Retrieved statement for create documents: {}", statement);

        EmailMessageDto emailMessage = EmailMessageDto.builder()
                .email(statement.getClient().getEmail())
                .statementId(statementUUID)
                .theme(Theme.CREATE_DOCUMENTS)
                .text("some text")
                .build();
        log.debug("Constructed create documents message: {}", emailMessage);

        kafkaProducer.send(topicCreateDocuments, emailMessage);
        log.info("Successfully sent create documents event for statement: {}", statementUUID);
    }

    public void sendKafkaSendDocuments(UUID statementUUID) {

        log.info("Starting to send documents event for statement: {}", statementUUID);
        Statement statement = statementRepository.findByStatementId(statementUUID)
                .orElseThrow(() -> new StatementNotFoundException(statementUUID));
        log.debug("Retrieved statement for send documents: {}", statement);

        EmailMessageDto emailMessage = EmailMessageDto.builder()
                .email(statement.getClient().getEmail())
                .statementId(statementUUID)
                .theme(Theme.SEND_DOCUMENTS)
                .text("some text")
                .build();

        log.debug("Constructed send documents message: {}", emailMessage);
        kafkaProducer.send(topicSendDocuments, emailMessage);
        log.info("Successfully sent documents for statement: {}", statementUUID);
    }

    public void sendKafkaSendSes(UUID statementUUID) {
        log.info("Starting to send SES event for statement: {}", statementUUID);

        Statement statement = statementRepository.findByStatementId(statementUUID)
                .orElseThrow(() -> new StatementNotFoundException(statementUUID));
        log.debug("Retrieved statement for SES: {}", statement);

        EmailMessageDto emailMessage = EmailMessageDto.builder()
                .email(statement.getClient().getEmail())
                .statementId(statementUUID)
                .theme(Theme.SEND_SES)
                .text("some text")
                .build();
        log.debug("Constructed SES message: {}", emailMessage);

        kafkaProducer.send(topicSendSes, emailMessage);
        log.info("Successfully sent SES event for statement: {}", statementUUID);
    }

    public void sendKafkaCreditIssued(UUID statementUUID) {

        log.info("Starting to send credit issued event for statement: {}", statementUUID);
        Statement statement = statementRepository.findByStatementId(statementUUID)
                .orElseThrow(() -> new StatementNotFoundException(statementUUID));
        log.debug("Retrieved statement for credit issued: {}", statement);

        EmailMessageDto emailMessage = EmailMessageDto.builder()
                .email(statement.getClient().getEmail())
                .statementId(statementUUID)
                .theme(Theme.CREDIT_ISSUED)
                .text("some text")
                .build();
        log.debug("Constructed credit issued message: {}", emailMessage);

        kafkaProducer.send(topicCreditIssued, emailMessage);
        log.info("Successfully sent credit issued event for statement: {}", statementUUID);
    }

    public void sendKafkaStatementDenied(UUID statementUUID) {

        log.info("Starting to send statement denied event for statement: {}", statementUUID);
        Statement statement = statementRepository.findByStatementId(statementUUID)
                .orElseThrow(() -> new StatementNotFoundException(statementUUID));
        log.debug("Retrieved statement for denied: {}", statement);

        EmailMessageDto emailMessage = EmailMessageDto.builder()
                .email(statement.getClient().getEmail())
                .statementId(statementUUID)
                .theme(Theme.STATEMENT_DENIED)
                .text("some text")
                .build();
        log.debug("Constructed statement denied message: {}", emailMessage);

        kafkaProducer.send(topicStatementDenied, emailMessage);
        log.info("Successfully sent statement denied event for statement: {}", statementUUID);
    }
}