package org.company.price.application.service;

import org.company.price.application.dto.PriceSearchCriteria;
import org.company.price.application.dto.PriceResponseDTO;
import org.company.price.application.mapper.PriceMapper;
import org.company.price.application.utils.ApplicationDateParser;
import org.company.price.domain.model.Price;
import org.company.price.domain.port.out.PriceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    PriceRepositoryPort repository;

    @Mock
    PriceMapper mapper;

    @InjectMocks
    PriceService service;

    @Captor
    ArgumentCaptor<PriceSearchCriteria> criteriaCaptor;

    @Test
    void getApplicablePrice_parsesDdMMyyyy_andReturnsDto() {
        Integer brandId = 1;
        Integer productId = 35455;
        String applicationDate = "14/06/2020 16:00:00";

        var applLocalDateTime = ApplicationDateParser.parse(applicationDate);

        Price price = mock(Price.class);
        PriceResponseDTO dto = new PriceResponseDTO(
                brandId,
                productId,
                1,
                applLocalDateTime,
                applLocalDateTime,
                new BigDecimal("35.50"),
                "EUR"
        );

        when(repository.findTopApplicablePrice(any())).thenReturn(Mono.just(price));
        when(mapper.toDto(price)).thenReturn(dto);

        StepVerifier.create(service.getApplicablePrice(brandId, productId, applicationDate))
                .expectNextMatches(r -> r.equals(dto))
                .verifyComplete();

        verify(repository).findTopApplicablePrice(criteriaCaptor.capture());
        PriceSearchCriteria captured = criteriaCaptor.getValue();
        assertEquals(brandId, captured.brandId());
        assertEquals(productId, captured.productId());
        assertEquals(applLocalDateTime, captured.applicationStart());
        assertEquals(applLocalDateTime, captured.applicationEnd());
    }

    @Test
    void getApplicablePrice_parsesIsoDateTime_andReturnsDto() {
        var brandId = 1;
        var productId = 35455;
        var applicationDate = "14/06/2020 16:00:00";

       var parsed = ApplicationDateParser.parse(applicationDate);

        var price = mock(Price.class);
        var dto = new PriceResponseDTO(
                brandId,
                productId,
                2,
                parsed,
                parsed,
                new BigDecimal("25.45"),
                "EUR"
        );

        when(repository.findTopApplicablePrice(any())).thenReturn(Mono.just(price));
        when(mapper.toDto(price)).thenReturn(dto);

        StepVerifier.create(service.getApplicablePrice(brandId, productId, applicationDate))
                .expectNextMatches(r -> r.equals(dto))
                .verifyComplete();

        verify(repository).findTopApplicablePrice(criteriaCaptor.capture());
        PriceSearchCriteria captured = criteriaCaptor.getValue();
        assertEquals(parsed, captured.applicationStart());
        assertEquals(parsed, captured.applicationEnd());
    }
}