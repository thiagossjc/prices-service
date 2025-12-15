package org.company.price.infrastructure.repository;

import org.company.price.infrastructure.repository.entity.PriceEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface PriceR2dbcRepository extends ReactiveCrudRepository<PriceEntity, Long> {

    Mono<PriceEntity> findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
            Integer brandId,
            Integer productId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}