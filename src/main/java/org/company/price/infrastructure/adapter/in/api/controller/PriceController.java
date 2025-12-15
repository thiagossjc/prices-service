package org.company.price.infrastructure.adapter.in.api.controller;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.company.price.application.port.PriceUseCasePort;

import org.company.price.infrastructure.adapter.in.api.PricesApi;
import org.company.price.infrastructure.adapter.in.api.error.ResourceNotFoundException;
import org.company.price.infrastructure.adapter.in.api.model.PriceResponse;
import org.company.price.infrastructure.mapper.openapi.PriceApiMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PriceController implements PricesApi {

    PriceUseCasePort priceUseCasePort;
    PriceApiMapper priceApiMapper;

    @Override
    public Mono<ResponseEntity<PriceResponse>> apiV1PricesGet(
            Integer brandId,
            Integer productId,
            String applicationDate,
            ServerWebExchange exchange
    ) {
        return priceUseCasePort.getApplicablePrice(brandId, productId, applicationDate)
                .map(priceApiMapper::toPriceResponse)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new ResourceNotFoundException("Applicable price not found for the given product, brand and date."))));
    }
}