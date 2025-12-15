package org.company.price.infrastructure.adapter.out;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.company.price.application.dto.PriceSearchCriteria;
import org.company.price.domain.model.Price;
import org.company.price.domain.port.out.PriceRepositoryPort;
import org.company.price.infrastructure.adapter.in.api.error.ServiceUnavailableException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Primary 
@Component
@RequiredArgsConstructor
public class PriceRepositoryCircuitBreakerProxy implements PriceRepositoryPort {

    private final PriceRepositoryAdapter priceRepositoryAdapterTarget; 
    private static final String CIRCUIT_BREAKER_NAME = "priceR2dbcCircuitBreaker";

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getFallbackPrice")
    public Mono<Price> findTopApplicablePrice(PriceSearchCriteria priceSearchCriteria) {
        return priceRepositoryAdapterTarget.findTopApplicablePrice(priceSearchCriteria);
    }
    
    public Mono<Price> getFallbackPrice(PriceSearchCriteria criteria, Throwable t) {
        var message = String.format("Circuit Breaker '%s' is open or encountered a persistent error while accessing R2DBC.", CIRCUIT_BREAKER_NAME);
        return Mono.error(new ServiceUnavailableException(message));
    }
}