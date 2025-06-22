package ru.neoflex.kubrak.deal.model.jsonb.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.neoflex.kubrak.deal.model.jsonb.Employment;

@Converter(autoApply = true)
public class EmploymentConverter implements AttributeConverter<Employment, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Employment employment) {

        try {
            return objectMapper.writeValueAsString(employment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting Employment to JSON", e);
        }
    }

    @Override
    public Employment convertToEntityAttribute(String dbData) {

        try {
            return objectMapper.readValue(dbData, Employment.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to Employment", e);
        }
    }
}
