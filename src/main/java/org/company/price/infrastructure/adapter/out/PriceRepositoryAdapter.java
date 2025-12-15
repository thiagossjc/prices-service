package org.company.price.infrastructure.adapter.out;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.company.price.application.dto.PriceSearchCriteria;
import org.company.price.domain.model.Price;
import org.company.price.infrastructure.mapper.rd2.PriceEntityMapper;
import org.company.price.infrastructure.repository.entity.PriceEntity;
import org.company.price.domain.port.out.PriceRepositoryPort;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PriceRepositoryAdapter implements PriceRepositoryPort {

    R2dbcEntityTemplate template;
    PriceEntityMapper priceEntityMapper;

    static final String CIRCUIT_BREAKER_NAME = "priceR2dbcCircuitBreaker";

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackFindTopApplicablePrice")
    public Mono<Price> findTopApplicablePrice(PriceSearchCriteria priceSearchCriteria) {

        var criteria = Criteria.where("brand_id").is(priceSearchCriteria.brandId())
                .and("product_id").is(priceSearchCriteria.productId())
                .and("start_date").lessThanOrEquals(priceSearchCriteria.applicationStart())
                .and("end_date").greaterThanOrEquals(priceSearchCriteria.applicationEnd());

        var query = Query.query(criteria)
                .sort(Sort.by(new Sort.Order(Sort.Direction.valueOf(priceSearchCriteria.orderByDirection()), priceSearchCriteria.orderByColumnName())))
                .limit(priceSearchCriteria.limit());

        return template.select(query, PriceEntity.class)
                .next()
                .map(priceEntityMapper::toPrice)
                .switchIfEmpty(Mono.empty());
    }
}