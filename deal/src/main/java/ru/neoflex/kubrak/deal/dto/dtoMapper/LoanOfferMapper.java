package ru.neoflex.kubrak.deal.dto.dtoMapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.model.jsonb.LoanOffer;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanOfferMapper {

    LoanOffer toEntity(LoanOfferDto dto);
    LoanOfferDto toDto(LoanOffer entity);
}
