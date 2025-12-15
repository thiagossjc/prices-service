package org.company.price.infrastructure.adapter.in.api.error;

public record ValidationError(
        String field,
        Object rejectedValue,
        String message
) {}
