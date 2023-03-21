package es.imaut.productapi.exception;

import lombok.Builder;

import java.util.stream.Stream;

@Builder
public record ProductApiErrorResponse(
    long timestamp,
    String path,
    int status,
    String error,
    String message,
    Stream<ProductApiError> errors) {

    @Builder
    public record ProductApiError(
            String defaultMessage,
            String objectName,
            String field,
            Object rejectedValue,
            String code) {
    }
}
