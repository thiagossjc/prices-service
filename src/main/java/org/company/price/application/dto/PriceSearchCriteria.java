package org.company.price.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record PriceSearchCriteria(
        Integer brandId,
        Integer productId,
        LocalDateTime applicationStart,
        LocalDateTime applicationEnd,
        String orderByDirection,
        String orderByColumnName,
        Integer limit
) {
}
