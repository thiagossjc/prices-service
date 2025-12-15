package org.company.price.infrastructure.mapper.openapi;

import org.company.price.application.dto.PriceResponseDTO;
import org.company.price.infrastructure.adapter.in.api.model.PriceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface PriceApiMapper {

    @Mapping(source = "startDate", target = "startDate", qualifiedByName = "formatDate")
    @Mapping(source = "endDate", target = "endDate", qualifiedByName = "formatDate")
    PriceResponse toPriceResponse(PriceResponseDTO dto);

    @Named("formatDate")
    static String formatDate(LocalDateTime dt) {
        return dt == null ? null : dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    static BigDecimal preserveTwoDecimals(BigDecimal bd) {
        return bd == null ? null : bd.setScale(2, RoundingMode.HALF_UP);
    }
}
