package ru.neoflex.kubrak.dossier.dto;

import lombok.*;
import ru.neoflex.kubrak.dossier.model.Theme;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageDto {

    String email;
    Theme theme;
    UUID statementId;
    String text;
}
