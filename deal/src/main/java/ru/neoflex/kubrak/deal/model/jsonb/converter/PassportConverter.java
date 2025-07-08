package ru.neoflex.kubrak.deal.model.jsonb.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.neoflex.kubrak.deal.model.jsonb.Passport;

@Converter(autoApply = true)
public class PassportConverter implements AttributeConverter<Passport, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Passport loanOffer) {

        try {
            return objectMapper.writeValueAsString(loanOffer);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting LoanOffer to JSON", e);
        }
    }

    @Override
    public Passport convertToEntityAttribute(String dbData) {

        try {
            return objectMapper.readValue(dbData, Passport.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to LoanOffer", e);
        }
    }
}
