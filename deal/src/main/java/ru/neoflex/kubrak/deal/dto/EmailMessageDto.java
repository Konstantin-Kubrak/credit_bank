package ru.neoflex.kubrak.deal.dto;

import lombok.Builder;
import lombok.Data;
import ru.neoflex.kubrak.deal.model.enums.Theme;

import java.util.UUID;

@Data
@Builder
public class EmailMessageDto {

    String email;
    Theme theme;
    UUID statementId;
    String text;
}