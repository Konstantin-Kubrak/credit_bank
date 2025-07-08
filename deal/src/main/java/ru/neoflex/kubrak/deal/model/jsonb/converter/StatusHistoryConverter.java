package ru.neoflex.kubrak.deal.model.jsonb.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.neoflex.kubrak.deal.model.jsonb.StatusHistory;

import java.util.List;

@Converter(autoApply = true)
public class StatusHistoryConverter implements AttributeConverter<List<StatusHistory>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(List<StatusHistory> attribute) {
        try {
            return attribute != null ? objectMapper.writeValueAsString(attribute) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting StatusHistory to JSON", e);
        }
    }

    @Override
    public List<StatusHistory> convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ?
                    objectMapper.readValue(dbData,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, StatusHistory.class)) :
                    null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to StatusHistory", e);
        }
    }
}
