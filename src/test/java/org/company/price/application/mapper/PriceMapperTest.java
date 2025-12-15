package org.company.price.application.mapper;

import org.company.price.domain.model.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PriceMapperTest {

    @Autowired
    private PriceMapper priceMapper;

    @Test
    @DisplayName("Should correctly map a domain Price model to a PriceResponseDTO")
    void shouldMapDomainPriceToPriceResponseDTO() {

        var startDate = LocalDateTime.of(2020, 6, 14, 0, 0, 0);
        var endDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

        var domainPrice = Price.builder()
                .brandId(1)
                .productId(35455)
                .priceList(1)
                .startDate(startDate)
                .endDate(endDate)
                .price(new BigDecimal("35.50"))
                .currency("EUR")
                .build();

        var dto = priceMapper.toDto(domainPrice);

        assertThat(dto).isNotNull();
        assertThat(dto.brandId()).isEqualTo(domainPrice.brandId());
        assertThat(dto.productId()).isEqualTo(domainPrice.productId());
        assertThat(dto.priceList()).isEqualTo(domainPrice.priceList());

        assertThat(dto.price()).isEqualByComparingTo(domainPrice.price());
        assertThat(dto.currency()).isEqualTo(domainPrice.currency());

        assertThat(dto.startDate()).isEqualTo(startDate);
        assertThat(dto.endDate()).isEqualTo(endDate);
    }

    @Test
    @DisplayName("Should correctly handle a null domain Price object")
    void shouldHandleNullDomainPrice() {
        var dto = priceMapper.toDto(null);

        assertThat(dto).isNull();
    }
}