package ru.neoflex.kubrak.dossier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.neoflex.kubrak.dossier.dto.EmailMessageDto;
import ru.neoflex.kubrak.dossier.exception.EmailException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DossierService {

    private final JavaMailSender mailSender;

    public void sendEmail(EmailMessageDto message, String defaultText) {
        log.debug("Preparing email for: {}", message.getEmail());
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(message.getEmail());
        mailMessage.setSubject(message.getTheme().toString());
        mailMessage.setText(message.getText() != null ? message.getText() : defaultText);

        try {
            mailSender.send(mailMessage);
            log.info("Email sent successfully to {}", message.getEmail());
        } catch (Exception e) {
            throw new EmailException(String.format("Failed to send email to %s: %s", message.getEmail(), e.getMessage()));
        }
    }
}
