package org.company.price.infrastructure.repository;

import org.company.price.infrastructure.repository.entity.PriceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest 
class PriceR2dbcRepositoryIntegrationTest {

    @Autowired
    private PriceR2dbcRepository priceRepository;

    @Autowired
    private R2dbcEntityTemplate entityTemplate;

    private final Integer BRAND_ID = 1;
    private final Integer PRODUCT_ID = 35455;
    private final LocalDateTime DATE_APP_1 = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
    private final LocalDateTime DATE_APP_2 = LocalDateTime.of(2020, 6, 14, 16, 0, 0);

    @BeforeEach
    void setUp() {
        // Arrange
        entityTemplate.delete(PriceEntity.class).all().block();

        var price1 = PriceEntity.builder()
                .id(1L).brandId(BRAND_ID).productId(PRODUCT_ID).priceList(1).priority(0)
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59))
                .price(new BigDecimal("35.50")).currency("EUR").build();

        var price2 = PriceEntity.builder()
                .id(2L).brandId(BRAND_ID).productId(PRODUCT_ID).priceList(2).priority(1) // PRIORIDAD ALTA
                .startDate(LocalDateTime.of(2020, 6, 14, 15, 0, 0))
                .endDate(LocalDateTime.of(2020, 6, 14, 18, 30, 0))
                .price(new BigDecimal("25.45")).currency("EUR").build();

        var price3 = PriceEntity.builder()
                .id(3L).brandId(BRAND_ID).productId(PRODUCT_ID).priceList(3).priority(0)
                .startDate(LocalDateTime.of(2020, 6, 15, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 6, 15, 11, 0, 0))
                .price(new BigDecimal("30.50")).currency("EUR").build();
        
        var price4 = PriceEntity.builder()
                .id(4L).brandId(BRAND_ID).productId(PRODUCT_ID).priceList(4).priority(2)
                .startDate(LocalDateTime.of(2021, 1, 1, 0, 0, 0))
                .endDate(LocalDateTime.of(2021, 1, 1, 23, 59, 59))
                .price(new BigDecimal("10.00")).currency("EUR").build();
        
        entityTemplate.insert(price1).block();
        entityTemplate.insert(price2).block();
        entityTemplate.insert(price3).block();
        entityTemplate.insert(price4).block();
    }

    @Test
    @DisplayName("Should return the single applicable price when no overlap exists (DATE_APP_1)")
    void shouldReturnApplicablePriceWithoutOverlap() {
        // Arrange

        // Act
        var resultMono = priceRepository.findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                BRAND_ID, PRODUCT_ID, DATE_APP_1, DATE_APP_1);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(price -> {
                    assertThat(price.getPriceList()).isEqualTo(1);
                    assertThat(price.getPriority()).isEqualTo(0);
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should return the price with HIGHEST priority when overlap exists (DATE_APP_2)")
    void shouldReturnHighestPriorityPriceWhenOverlap() {
        // Arrange
        
        // Act
        Mono<PriceEntity> resultMono = priceRepository.findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                BRAND_ID, PRODUCT_ID, DATE_APP_2, DATE_APP_2);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(price -> {
                    assertThat(price.getPriceList()).isEqualTo(2);
                    assertThat(price.getPriority()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty Mono when no price is applicable for the date")
    void shouldReturnEmptyWhenNoApplicablePrice() {
        // Arrange
        var dateOutsideRange = LocalDateTime.of(2020, 6, 13, 10, 0, 0);

        // Act
        var resultMono = priceRepository.findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                BRAND_ID, PRODUCT_ID, dateOutsideRange, dateOutsideRange);

        // Assert
        StepVerifier.create(resultMono)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should return empty Mono when BrandId or ProductId does not match")
    void shouldReturnEmptyWhenCriteriaMismatch() {
        // Arrange
        var nonExistentBrandId = 99;

        // Act
        var resultMono = priceRepository.findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                nonExistentBrandId, PRODUCT_ID, DATE_APP_1, DATE_APP_1);

        // Assert
        StepVerifier.create(resultMono)
                .verifyComplete();
    }
}