package org.company.price.infrastructure.mapper.rd2;

import org.company.price.domain.model.Price;
import org.company.price.infrastructure.repository.entity.PriceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriceEntityMapper {

    Price toPrice(PriceEntity priceEntity);
}