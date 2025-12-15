package org.company.price.infrastructure.adapter.mapper.entity;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.company.price.infrastructure.mapper.rd2.PriceEntityMapper;
import org.company.price.infrastructure.mapper.rd2.PriceEntityMapperImpl;
import org.company.price.infrastructure.repository.entity.PriceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = PriceEntityMapperImpl.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class PriceEntityMapperTest {

    @Autowired
    PriceEntityMapper priceEntityMapper;

    @Test
    @DisplayName("Should correctly map all fields from PriceEntity to Domain Price model")
    void shouldMapPriceEntityToDomainPrice() {
        // Arrange
        var startDate = LocalDateTime.of(2025, 12, 14, 10, 0, 0);
        var endDate = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
        var priceValue = new BigDecimal("45.99");

        var entity = PriceEntity.builder()
                .id(1001L)
                .brandId(1)
                .productId(35455)
                .priceList(2)
                .startDate(startDate)
                .endDate(endDate)
                .priority(1)
                .price(priceValue)
                .currency("EUR")
                .build();

        // Act
        var domainModel = priceEntityMapper.toPrice(entity);

        // Assert
        assertAll("PriceEntity to Price Domain Model Mapping",
            () -> assertThat(domainModel.brandId()).isEqualTo(entity.getBrandId()),
            () -> assertThat(domainModel.productId()).isEqualTo(entity.getProductId()),
            () -> assertThat(domainModel.priceList()).isEqualTo(entity.getPriceList()),
            () -> assertThat(domainModel.startDate()).isEqualTo(entity.getStartDate()),
            () -> assertThat(domainModel.endDate()).isEqualTo(entity.getEndDate()),
            () -> assertThat(domainModel.priority()).isEqualTo(entity.getPriority()),
            
            () -> assertThat(domainModel.price()).isEqualByComparingTo(entity.getPrice()),
            
            () -> assertThat(domainModel.currency()).isEqualTo(entity.getCurrency())
        );
    }
    
    @Test
    @DisplayName("Should handle null PriceEntity input gracefully")
    void shouldHandleNullInput() {
        // Act
        var domainModel = priceEntityMapper.toPrice(null);

        // Assert
        assertThat(domainModel).isNull();
    }
}