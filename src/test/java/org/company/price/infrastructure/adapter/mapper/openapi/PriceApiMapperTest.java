package org.company.price.infrastructure.adapter.mapper.openapi;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.company.price.application.dto.PriceResponseDTO;
import org.company.price.infrastructure.mapper.openapi.PriceApiMapper;
import org.company.price.infrastructure.mapper.openapi.PriceApiMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = PriceApiMapperImpl.class)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class PriceApiMapperTest {

    @Autowired
    @NonFinal
    PriceApiMapper priceApiMapper;

    @NonFinal
    PriceResponseDTO priceDto;
    LocalDateTime testDateTime = LocalDateTime.of(2025, 12, 15, 10, 30, 0);
    BigDecimal testPrice = new BigDecimal("35.50");

    @BeforeEach
    void setUp() {
        priceDto = PriceResponseDTO.builder()
                .brandId(1)
                .productId(35455)
                .priceList(1)
                .startDate(testDateTime)
                .endDate(testDateTime.plusDays(1))
                .price(testPrice)
                .currency("EUR")
                .build();
    }

    @Test
    @DisplayName("Should correctly map PriceResponseDTO fields to PriceResponse")
    void shouldMapDtoToResponse() {
        // Act
        var response = priceApiMapper.toPriceResponse(priceDto);

        // Assert
        assertAll("Mapping simple fields",
                () -> assertThat(response.getBrandId()).isEqualTo(priceDto.brandId()),
                () -> assertThat(response.getProductId()).isEqualTo(priceDto.productId()),
                () -> assertThat(response.getPriceList()).isEqualTo(priceDto.priceList()),
                () -> assertThat(response.getPrice()).isEqualTo(priceDto.price().doubleValue()),
                () -> assertThat(response.getCurrency()).isEqualTo(priceDto.currency())
        );
    }

    @Test
    @DisplayName("Should correctly format LocalDateTime to dd/MM/yyyy")
    void shouldFormatDate() {
        // Act
        var response = priceApiMapper.toPriceResponse(priceDto);

        // Assert
        var expectedDate = "15/12/2025 10:30:00";
        var expectedNextDate = "16/12/2025 10:30:00";

        assertAll("Date formatting",
                () -> assertThat(response.getStartDate()).isEqualTo(expectedDate),
                () -> assertThat(response.getEndDate()).isEqualTo(expectedNextDate)
        );
    }
    
    @Test
    @DisplayName("Should return null for null dates")
    void shouldHandleNullDates() {
        // Arrange
        var dtoWithNulls = priceDto.toBuilder()
                .startDate(null)
                .endDate(null)
                .build();
        
        // Act
        var response = priceApiMapper.toPriceResponse(dtoWithNulls);

        // Assert
        assertAll("Null date handling",
                () -> assertThat(response.getStartDate()).isNull(),
                () -> assertThat(response.getEndDate()).isNull()
        );
    }

    @Test
    @DisplayName("Should preserve two decimals using HALF_UP rounding")
    void shouldPreserveTwoDecimals() {
        // Arrange
        var valueToRound = new BigDecimal("12.345");
        var expectedValue = new BigDecimal("12.35").setScale(2, RoundingMode.HALF_UP);

        // Act
        var result = PriceApiMapper.preserveTwoDecimals(valueToRound);

        // Assert
        assertThat(result).isEqualByComparingTo(expectedValue);
        
        // Arrange
        var valueExact = new BigDecimal("100.50");
        
        // Act
        var resultExact = PriceApiMapper.preserveTwoDecimals(valueExact);

        // Assert
        assertThat(resultExact).isEqualByComparingTo(valueExact.setScale(2, RoundingMode.HALF_UP));
    }
}