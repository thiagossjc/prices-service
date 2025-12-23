package org.company.price.infrastructure.adapter.in.api.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.company.price.domain.exception.PriceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
@FieldDefaults(level = AccessLevel.PRIVATE)
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    GlobalExceptionHandler handler;

    @Mock
    ServerWebExchange exchange;
    
    @Mock
    private org.springframework.http.server.reactive.ServerHttpRequest request;
    @Mock
    private org.springframework.http.server.RequestPath path;
    
    static final String REQUEST_PATH = "/api/prices";
    static final String TRACE_ID_HEADER = "1234-test-trace";

    @BeforeEach
    void setUp() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getPath()).thenReturn(path);
        when(path.value()).thenReturn(REQUEST_PATH);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", TRACE_ID_HEADER);
        when(request.getHeaders()).thenReturn(headers);
    }

    @Test
    @DisplayName("Should handle WebExchangeBindException and return 400 BAD_REQUEST with field errors")
    void shouldHandleBindException() {
        // Arrange
        var fieldError = new FieldError("priceDto", "brandId", "Value 0", false, null, null, "must be greater than 0");
        var ex = mock(WebExchangeBindException.class);
        when(ex.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        Mono<ResponseEntity<ProblemDetails>> resultMono = handler.handleBindException(ex, exchange);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/problem+json"));
                    assertThat(response.getBody()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException and return 400 BAD_REQUEST with constraint violations")
    void shouldHandleConstraintViolation() {
        // Arrange
        ConstraintViolation<?> cv = mock(ConstraintViolation.class);
        when(cv.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(cv.getPropertyPath().toString()).thenReturn("find.paramName");
        when(cv.getInvalidValue()).thenReturn("invalid_value");
        when(cv.getMessage()).thenReturn("must be valid format");
        
        var ex = new ConstraintViolationException(Set.of(cv));

        // Act
        Mono<ResponseEntity<ProblemDetails>> resultMono = handler.handleConstraintViolation(ex, exchange);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/problem+json"));
                    assertThat(response.getBody()).isNotNull();
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should handle ResourceNotFoundException and return 404 NOT_FOUND")
    void shouldHandleNotFoundException() {
        // Arrange
        var ex = new PriceNotFoundException("Price not found for criteria");

        // Act
        Mono<ResponseEntity<ProblemDetails>> resultMono = handler.handlePriceNotFound(ex, exchange);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/problem+json"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle ServiceUnavailableException and return 503 SERVICE_UNAVAILABLE")
    void shouldHandleServiceUnavailable() {
        // Arrange
        var ex = new ServiceUnavailableException("Circuit Breaker is open");

        // Act
        Mono<ResponseEntity<ProblemDetails>> resultMono = handler.handleServiceUnavailable(ex, exchange);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/problem+json"));
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should handle generic Exception and return 500 INTERNAL_SERVER_ERROR")
    void shouldHandleGenericException() {
        // Arrange
        Exception ex = new RuntimeException("DB connection failed unexpectedly");

        // Act
        Mono<ResponseEntity<ProblemDetails>> resultMono = handler.handleException(ex, exchange);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/problem+json"));
                })
                .verifyComplete();
    }
}