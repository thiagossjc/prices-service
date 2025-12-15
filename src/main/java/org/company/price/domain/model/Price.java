package org.company.price.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

    @Builder(toBuilder = true)
    public record Price(
            Integer brandId,
            Integer productId,
            Integer priceList,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer priority,
            BigDecimal price,
            String currency
    ) {}