package org.company.price.infrastructure.adapter.in.api.error;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}