package es.imaut.clientapi.exception;

import lombok.Builder;

import java.util.stream.Stream;

@Builder
public record ClientApiErrorResponse(
    long timestamp,
    String path,
    int status,
    String error,
    String message,
    Stream<ClientApiError> errors) {

    @Builder
    public record ClientApiError(
            String defaultMessage,
            String objectName,
            String field,
            Object rejectedValue,
            String code) {
    }
}
