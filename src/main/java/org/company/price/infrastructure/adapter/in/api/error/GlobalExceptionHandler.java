package org.company.price.infrastructure.adapter.in.api.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.company.price.domain.exception.PriceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(PriceNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetails>> handlePriceNotFound(
            PriceNotFoundException ex,
            ServerWebExchange exchange
    ) {
        var traceId = traceId(exchange);

        log.info("Price not found at path={} traceId={} message={}",
                exchange.getRequest().getPath(), traceId, ex.getMessage());

        var problem = new ProblemDetails(
                "https://example.com/probs/not-found",
                "Not Found",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                exchange.getRequest().getPath().value(),
                OffsetDateTime.now().toString(),
                traceId,
                List.of()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.valueOf("application/problem+json"))
                .body(problem));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleBindException(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        var traceId = traceId(exchange);

        log.warn("Validation error (BindException) at path={} traceId={} errors={}",
                exchange.getRequest().getPath(), traceId, ex.getFieldErrors());

        var errors = ex.getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        var problem = new ProblemDetails(
                "https://example.com/probs/validation",
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                "One or more parameters are invalid.",
                exchange.getRequest().getPath().value(),
                OffsetDateTime.now().toString(),
                traceId,
                errors
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.valueOf("application/problem+json"))
                .body(problem));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleConstraintViolation(
            ConstraintViolationException ex,
            ServerWebExchange exchange
    ) {
        var traceId = traceId(exchange);

        log.warn("Constraint violation at path={} traceId={} violations={}",
                exchange.getRequest().getPath(), traceId, ex.getConstraintViolations());

        var errors = ex.getConstraintViolations().stream()
                .map(cv -> new ValidationError(cv.getPropertyPath().toString(), cv.getInvalidValue(), cv.getMessage()))
                .collect(Collectors.toList());

        var problem = new ProblemDetails(
                "https://example.com/probs/validation",
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                "One or more constraints were violated.",
                exchange.getRequest().getPath().value(),
                OffsetDateTime.now().toString(),
                traceId,
                errors
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.valueOf("application/problem+json"))
                .body(problem));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetails>> handleException(
            Exception ex,
            ServerWebExchange exchange
    ) {
        var traceId = traceId(exchange);

        log.error("Unexpected error at path={} traceId={}", exchange.getRequest().getPath(), traceId, ex);

        var problem = new ProblemDetails(
                "https://example.com/probs/internal",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
                exchange.getRequest().getPath().value(),
                OffsetDateTime.now().toString(),
                traceId,
                List.of()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.valueOf("application/problem+json"))
                .body(problem));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleServiceUnavailable(
            ServiceUnavailableException ex,
            ServerWebExchange exchange
    ) {
        var traceId = traceId(exchange);

        log.warn("Service unavailable at path={} traceId={} message={}",
                exchange.getRequest().getPath(), traceId, ex.getMessage());

        var problem = new ProblemDetails(
                "https://example.com/probs/service-unavailable",
                "Service Unavailable",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                exchange.getRequest().getPath().value(),
                OffsetDateTime.now().toString(),
                traceId,
                List.of()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.valueOf("application/problem+json"))
                .body(problem));
    }

    private String traceId(ServerWebExchange exchange) {
        var header = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        return header != null && !header.isBlank() ? header : UUID.randomUUID().toString();
    }
}