package org.company.price.application.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.company.price.application.dto.PriceResponseDTO;
import org.company.price.application.dto.PriceSearchCriteria;
import org.company.price.application.mapper.PriceMapper;
import org.company.price.application.port.PriceUseCasePort;
import org.company.price.application.utils.ApplicationDateParser;
import org.company.price.domain.port.out.PriceRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PriceService implements PriceUseCasePort {

    PriceRepositoryPort repository;
    PriceMapper mapper;
    static String ORDER_BY_DIRECTION_DESC = "DESC";
    static String ORDER_BY_COLUMN_PRIORITY = "priority";

    @Override
    public Mono<PriceResponseDTO> getApplicablePrice(Integer brandId, Integer productId, String applicationDate) {

        var applicationLocalDateTime = ApplicationDateParser.parse(applicationDate);

        var priceSearchCriteria=  PriceSearchCriteria.builder()
                .productId(productId)
                .brandId(brandId)
                .applicationStart(applicationLocalDateTime)
                .applicationEnd(applicationLocalDateTime)
                .orderByDirection(ORDER_BY_DIRECTION_DESC)
                .orderByColumnName(ORDER_BY_COLUMN_PRIORITY)
                .limit(1)
                .build();

        return repository.findTopApplicablePrice(priceSearchCriteria)
                .map(mapper::toDto);
    }
}
