package ru.neoflex.kubrak.dossier.kafka;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.kubrak.dossier.dto.EmailMessageDto;
import ru.neoflex.kubrak.dossier.service.DossierService;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceBaseTest{

    @Mock
    protected DossierService dossierService;

    @InjectMocks
    protected KafkaConsumerService kafkaConsumerService;

    protected EmailMessageDto createTestMessage() {
        return EmailMessageDto.builder()
                .email("test@example.com")
                .statementId(UUID.randomUUID())
                .build();
    }
}