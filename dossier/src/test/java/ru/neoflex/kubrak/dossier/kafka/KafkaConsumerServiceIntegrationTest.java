package ru.neoflex.kubrak.dossier.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.neoflex.kubrak.dossier.dto.EmailMessageDto;
import ru.neoflex.kubrak.dossier.service.DossierService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class KafkaConsumerServiceIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.0")
    );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, EmailMessageDto> kafkaTemplate;

    @MockitoSpyBean
    private KafkaConsumerService kafkaConsumerService;

    @MockitoBean
    private DossierService dossierService;

    @Test
    void whenSendMessageToKafka_thenConsumerShouldProcessIt() throws Exception {
        doNothing().when(dossierService).sendEmail(any(), any());

        EmailMessageDto message = EmailMessageDto.builder()
                .email("test@example.com")
                .statementId(UUID.randomUUID())
                .build();

        kafkaTemplate.send("finish-registration", message).get();

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(kafkaConsumerService).handleFinishRegistration(any());
            verify(dossierService).sendEmail(any(), any());
        });
    }
}