package ru.neoflex.kubrak.deal.model.jsonb.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.neoflex.kubrak.deal.model.jsonb.PaymentScheduleElement;

import java.util.List;

@Converter(autoApply = true)
public class PaymentScheduleConverter implements AttributeConverter<List<PaymentScheduleElement>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(List<PaymentScheduleElement> attribute) {
        try {
            return attribute != null ? objectMapper.writeValueAsString(attribute) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting PaymentScheduleElement to JSON", e);
        }
    }

    @Override
    public List<PaymentScheduleElement> convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ?
                    objectMapper.readValue(dbData,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, PaymentScheduleElement.class)) :
                    null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to PaymentScheduleElement", e);
        }
    }
}
