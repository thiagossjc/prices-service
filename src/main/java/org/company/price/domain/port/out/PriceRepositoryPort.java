package org.company.price.domain.port.out;

import org.company.price.application.dto.PriceSearchCriteria;
import org.company.price.domain.model.Price;
import reactor.core.publisher.Mono;

public interface PriceRepositoryPort {

    Mono<Price> findTopApplicablePrice(PriceSearchCriteria priceSearchCriteria);

}