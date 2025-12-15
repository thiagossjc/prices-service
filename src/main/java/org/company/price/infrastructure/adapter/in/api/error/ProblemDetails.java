package org.company.price.infrastructure.adapter.in.api.error;

import java.util.List;

public record ProblemDetails(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String timestamp,
        String traceId,
        List<ValidationError> errors
) {}
