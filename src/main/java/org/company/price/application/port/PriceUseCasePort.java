package org.company.price.application.port;

import org.company.price.application.dto.PriceResponseDTO;
import reactor.core.publisher.Mono;

public interface PriceUseCasePort {
    Mono<PriceResponseDTO> getApplicablePrice(Integer brandId, Integer productId, String applicationDate);
}
