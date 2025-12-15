package org.company.price.infrastructure.repository.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PriceEntityUnitTest {

    // Arrange
    private final LocalDateTime testStart = LocalDateTime.of(2025, 12, 1, 0, 0);
    private final LocalDateTime testEnd = LocalDateTime.of(2025, 12, 31, 23, 59);
    private final BigDecimal testPrice = new BigDecimal("99.99");

    private PriceEntity createBaseEntity() {
        return PriceEntity.builder()
                .id(1L)
                .brandId(1)
                .productId(35455)
                .priceList(4)
                .startDate(testStart)
                .endDate(testEnd)
                .priority(1)
                .price(testPrice)
                .currency("EUR")
                .build();
    }

    @Test
    @DisplayName("Should correctly create an instance using the Builder pattern and verify fields")
    void shouldCreateInstanceUsingBuilder() {
        // Arrange
        var entity = createBaseEntity();

        // Act & Assert
        assertNotNull(entity);
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getBrandId()).isEqualTo(1);
        assertThat(entity.getProductId()).isEqualTo(35455);
        assertThat(entity.getStartDate()).isEqualTo(testStart);
        assertThat(entity.getPrice()).isEqualByComparingTo(testPrice);
        assertThat(entity.getCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Should correctly handle the NoArgsConstructor")
    void shouldHandleNoArgsConstructor() {
        // Arrange
        var entity = new PriceEntity();

        // Act & Assert
        assertNotNull(entity);
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("Should have correct equals and hashCode implementation (Lombok @Data)")
    void shouldVerifyEqualsAndHashCode() {
        // Arrange
        var entity1 = createBaseEntity();
        var entity2 = createBaseEntity();
        
        var entity3 = createBaseEntity();
        entity3.setId(2L); 

        // Act & Assert
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        
        assertNotEquals(entity1, entity3);
        assertNotEquals(entity1.hashCode(), entity3.hashCode());
    }
}