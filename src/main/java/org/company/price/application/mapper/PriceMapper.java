package org.company.price.application.mapper;

import org.company.price.application.dto.PriceResponseDTO;
import org.company.price.domain.model.Price;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriceMapper {

    PriceResponseDTO toDto(Price price);
}
