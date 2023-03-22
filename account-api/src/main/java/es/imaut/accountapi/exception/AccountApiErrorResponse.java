package es.imaut.accountapi.exception;

import lombok.Builder;

import java.util.stream.Stream;

@Builder
public record AccountApiErrorResponse(
    long timestamp,
    String path,
    int status,
    String error,
    String message,
    Stream<AccountApiError> errors) {

    @Builder
    public record AccountApiError(
            String defaultMessage,
            String objectName,
            String field,
            Object rejectedValue,
            String code) {
    }
}
