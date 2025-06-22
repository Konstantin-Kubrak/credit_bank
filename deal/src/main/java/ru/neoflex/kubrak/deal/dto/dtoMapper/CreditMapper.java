package ru.neoflex.kubrak.deal.dto.dtoMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.neoflex.kubrak.deal.dto.CreditDto;
import ru.neoflex.kubrak.deal.dto.PaymentScheduleElementDto;
import ru.neoflex.kubrak.deal.model.entity.Credit;
import ru.neoflex.kubrak.deal.model.jsonb.PaymentScheduleElement;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CreditMapper {

    @Mapping(target = "creditId", ignore = true)
    @Mapping(target = "creditStatus", ignore = true)
    @Mapping(target = "insuranceEnabled", source = "isInsuranceEnabled")
    @Mapping(target = "salaryClient", source = "isSalaryClient")
    @Mapping(target = "paymentSchedule", source = "paymentSchedule", qualifiedByName = "mapPaymentSchedule")
    Credit toEntity(CreditDto dto);

    @Mapping(target = "isInsuranceEnabled", source = "insuranceEnabled")
    @Mapping(target = "isSalaryClient", source = "salaryClient")
    @Mapping(target = "paymentSchedule", source = "paymentSchedule", qualifiedByName = "mapPaymentScheduleDto")
    CreditDto toDto(Credit entity);

    @Named("mapPaymentSchedule")
    default List<PaymentScheduleElement> mapPaymentSchedule(List<PaymentScheduleElementDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toPaymentElement).toList();
    }

    @Named("mapPaymentScheduleDto")
    default List<PaymentScheduleElementDto> mapPaymentScheduleDto(List<PaymentScheduleElement> elements) {
        if (elements == null) return null;
        return elements.stream().map(this::toPaymentElementDto).toList();
    }

    PaymentScheduleElement toPaymentElement(PaymentScheduleElementDto dto);

    PaymentScheduleElementDto toPaymentElementDto(PaymentScheduleElement entity);

}