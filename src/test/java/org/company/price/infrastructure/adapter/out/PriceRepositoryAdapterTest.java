package org.company.price.infrastructure.adapter.out;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.company.price.application.dto.PriceSearchCriteria;
import org.company.price.domain.model.Price;
import org.company.price.infrastructure.mapper.rd2.PriceEntityMapper;
import org.company.price.infrastructure.repository.entity.PriceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class PriceRepositoryAdapterTest {

    @Mock
    R2dbcEntityTemplate template;

    @Mock
    PriceEntityMapper priceEntityMapper;

    @InjectMocks
    PriceRepositoryAdapter priceRepositoryAdapter;

    PriceSearchCriteria criteria;
    PriceEntity mockEntity;
    Price expectedDomain;

    @BeforeEach
    void setUp() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 15, 10, 0, 0);

        criteria = new PriceSearchCriteria(
                1, 
                35455, 
                applicationDate, 
                applicationDate,
                "DESC",
                "priority",
                1
        );

        mockEntity = PriceEntity.builder()
                .brandId(1).productId(35455).price(new BigDecimal("30.50")).build();

        expectedDomain = Price.builder()
                .brandId(1).productId(35455).price(new BigDecimal("30.50")).build();
    }

    @Test
    @DisplayName("Should return the top applicable price when found in the database")
    void findTopApplicablePrice_Found() {
        //Arrange
        when(template.select(any(Query.class), eq(PriceEntity.class)))
                .thenReturn(Flux.just(mockEntity));
        
        when(priceEntityMapper.toPrice(eq(mockEntity)))
                .thenReturn(expectedDomain);

        // Act
        var result = priceRepositoryAdapter.findTopApplicablePrice(criteria);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedDomain)
                .verifyComplete();

        verify(template).select(any(Query.class), eq(PriceEntity.class));
    }
    
    @Test
    @DisplayName("Should return Mono.empty when no applicable price is found")
    void findTopApplicablePrice_NotFound() {
        // Arrange
        when(template.select(any(Query.class), eq(PriceEntity.class)))
                .thenReturn(Flux.empty());

        // Act
        var result = priceRepositoryAdapter.findTopApplicablePrice(criteria);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(priceEntityMapper, org.mockito.Mockito.never()).toPrice(any(PriceEntity.class));
    }
}