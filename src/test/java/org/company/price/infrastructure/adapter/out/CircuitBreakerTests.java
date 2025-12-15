package org.company.price.infrastructure.adapter.out;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.company.price.application.dto.PriceSearchCriteria;
import org.company.price.domain.port.out.PriceRepositoryPort;
import org.company.price.infrastructure.adapter.in.api.error.ServiceUnavailableException;
import org.company.price.infrastructure.mapper.rd2.PriceEntityMapper;
import org.company.price.infrastructure.repository.entity.PriceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
@SpringBootTest(classes = CircuitBreakerTest.TestConfig.class)
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.priceR2dbcCircuitBreaker.failureRateThreshold=1",
        "resilience4j.circuitbreaker.instances.priceR2dbcCircuitBreaker.minimumNumberOfCalls=1",
        "resilience4j.circuitbreaker.instances.priceR2dbcCircuitBreaker.waitDurationInOpenState=1s",
        "resilience4j.circuitbreaker.instances.priceR2dbcCircuitBreaker.slidingWindowSize=1",
        "resilience4j.circuitbreaker.instances.priceR2dbcCircuitBreaker.recordExceptions=java.lang.RuntimeException"
})
class CircuitBreakerTest {

    @Configuration
    @ComponentScan(basePackages = {
            "org.company.price.infrastructure.adapter.out",
            "io.github.resilience4j.circuitbreaker.autoconfigure"
    })
    @EnableAutoConfiguration(exclude = {
            R2dbcAutoConfiguration.class,
            R2dbcDataAutoConfiguration.class,
            R2dbcRepositoriesAutoConfiguration.class
    })
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class TestConfig {}

    @Autowired
    private PriceRepositoryPort priceRepositoryPort;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private R2dbcEntityTemplate template;

    @MockBean
    private PriceEntityMapper priceEntityMapper;

    @Test
    @DisplayName("Should open circuit after failure and then return ServiceUnavailableException from fallback")
    void shouldOpenCircuitAndCallFallback() {

        // Arrange
        var applicationDate = LocalDateTime.of(2020, 6, 15, 10, 0, 0);

        var criteria = new PriceSearchCriteria(
                1, 35455, applicationDate, applicationDate,
                "DESC", "priority", 1
        );

        when(template.select(any(Query.class), eq(PriceEntity.class)))
                .thenReturn(Flux.error(new RuntimeException("Simulated DB error")));

        // Act (First call - fails, opens Circuit Breaker)
        StepVerifier.create(priceRepositoryPort.findTopApplicablePrice(criteria))
                .expectError(RuntimeException.class)
                .verify();

        // Assert (Check state transition)
        CircuitBreaker cb =
                circuitBreakerRegistry.circuitBreaker("priceR2dbcCircuitBreaker");

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Act (Second call - Circuit Breaker is open, should skip execution and call fallback)
        StepVerifier.create(priceRepositoryPort.findTopApplicablePrice(criteria))
                .expectError(ServiceUnavailableException.class)
                .verify();

        // Assert (Verify interactions)
        verify(template, times(2))
                .select(any(Query.class), eq(PriceEntity.class));

        verifyNoInteractions(priceEntityMapper);
    }
}