package org.company.price.infrastructure.adapter.in.api.controller;

import org.company.price.application.dto.PriceResponseDTO;
import org.company.price.application.port.PriceUseCasePort;
import org.company.price.application.utils.ApplicationDateParser;
import org.company.price.infrastructure.adapter.in.api.error.GlobalExceptionHandler;
import org.company.price.infrastructure.adapter.in.api.model.PriceResponse;
import org.company.price.infrastructure.mapper.openapi.PriceApiMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PriceControllerTest {

    @Mock
    PriceUseCasePort priceUseCasePort;

    @Mock
    PriceApiMapper priceApiMapper;

    @InjectMocks
    PriceController priceController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        // Arrange
        var globalExceptionHandler = new GlobalExceptionHandler();

        webTestClient = WebTestClient.bindToController(priceController)
                .controllerAdvice(globalExceptionHandler)
                .build();
    }

    static Stream<Arguments> testCases() {
        return Stream.of(
                arguments("14/06/2020 10:00:00", 1, 35.50),
                arguments("14/06/2020 16:00:00", 2, 25.45),
                arguments("14/06/2020 21:00:00", 1, 35.50),
                arguments("15/06/2020 10:00:00", 3, 30.50),
                arguments("16/06/2020 21:00:00", 4, 38.95)
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void shouldReturnApplicablePriceForGivenDateProductAndBrand(String dateIso,
                                                                Integer expectedPriceList,
                                                                Double expectedPrice) {
        // Arrange
        var brandId = 1;
        var productId = 35455;
        var date = ApplicationDateParser.parse(dateIso);

        var mockDto = PriceResponseDTO.builder()
                .brandId(brandId)
                .productId(productId)
                .priceList(expectedPriceList)
                .startDate(date.minusHours(1))
                .endDate(date.plusHours(1))
                .price(BigDecimal.valueOf(expectedPrice))
                .currency("EUR")
                .build();

        var mockResponse = new PriceResponse();
        mockResponse.setBrandId(brandId);
        mockResponse.setProductId(productId);
        mockResponse.setPriceList(expectedPriceList);
        mockResponse.setStartDate(mockDto.startDate().toString());
        mockResponse.setEndDate(mockDto.endDate().toString());
        mockResponse.setPrice(expectedPrice);
        mockResponse.setCurrency("EUR");

        when(priceUseCasePort.getApplicablePrice(anyInt(), anyInt(), any(String.class)))
                .thenReturn(Mono.just(mockDto));
        when(priceApiMapper.toPriceResponse(mockDto)).thenReturn(mockResponse);

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/prices")
                        .queryParam("brandId", brandId)
                        .queryParam("productId", productId)
                        .queryParam("applicationDate", date.toString())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PriceResponse.class)
                .consumeWith(result -> {
                    // Assert
                    var body = result.getResponseBody();
                    assert body != null;
                    assertThat(body.getBrandId()).isEqualTo(brandId);
                    assertThat(body.getProductId()).isEqualTo(productId);
                    assertThat(body.getPriceList()).isEqualTo(expectedPriceList);
                    assertThat(body.getPrice()).isEqualTo(expectedPrice);
                    assertThat(body.getCurrency()).isEqualTo(mockDto.currency());
                });

        // Assert
        verify(priceUseCasePort).getApplicablePrice(eq(brandId), eq(productId), any(String.class));
        verify(priceApiMapper).toPriceResponse(mockDto);
    }

    @Test
    void shouldReturn404NotFoundWhenNoApplicablePriceIsFound() {
        // Arrange
        var brandId = 99;
        var productId = 99999;
        var dateIso = "15/06/2020 10:00:00";

        when(priceUseCasePort.getApplicablePrice(anyInt(), anyInt(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/prices")
                        .queryParam("brandId", brandId)
                        .queryParam("productId", productId)
                        .queryParam("applicationDate", dateIso)
                        .build())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectHeader().contentType("application/problem+json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("Applicable price not found for the given product, brand and date.")
                .jsonPath("$.title").isEqualTo("Not Found");

        // Assert
        verify(priceUseCasePort).getApplicablePrice(eq(brandId), eq(productId), any(String.class));
        verifyNoInteractions(priceApiMapper);
    }
}