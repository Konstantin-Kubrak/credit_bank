package ru.neoflex.kubrak.dossier.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import ru.neoflex.kubrak.dossier.dto.EmailMessageDto;
import ru.neoflex.kubrak.dossier.exception.EmailException;
import ru.neoflex.kubrak.dossier.model.Theme;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DossierServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private DossierService dossierService;

    @Test
    void sendEmail_ShouldSuccess_WhenAllFieldsValid() {
        EmailMessageDto message = EmailMessageDto.builder()
                .email("test@example.com")
                .theme(Theme.FINISH_REGISTRATION)
                .text("Custom text")
                .build();

        dossierService.sendEmail(message, "Default text");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_ShouldUseDefaultText_WhenTextIsNull() {
        EmailMessageDto message = EmailMessageDto.builder()
                .email("test@example.com")
                .theme(Theme.FINISH_REGISTRATION)
                .build();

        dossierService.sendEmail(message, "Default text");

        verify(mailSender, times(1)).send(argThat((SimpleMailMessage mail) ->
                mail.getText().equals("Default text")
        ));
    }

    @Test
    void sendEmail_ShouldThrowEmailException_WhenSendingFails() {
        EmailMessageDto message = EmailMessageDto.builder()
                .email("test@example.com")
                .theme(Theme.FINISH_REGISTRATION)
                .build();

        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailException.class, () ->
                dossierService.sendEmail(message, "Default text")
        );
    }
}