package ru.neoflex.kubrak.deal.dto.dtoMapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.neoflex.kubrak.deal.dto.EmploymentDto;
import ru.neoflex.kubrak.deal.model.jsonb.Employment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmploymentMapper {

    Employment toEntity(EmploymentDto dto);
    EmploymentDto toDto(Employment entity);
}