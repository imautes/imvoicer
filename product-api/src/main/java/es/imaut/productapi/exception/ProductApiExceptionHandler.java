package es.imaut.productapi.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import static java.beans.Introspector.decapitalize;
import static java.lang.String.valueOf;
import static java.time.Instant.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@RequiredArgsConstructor
public class ProductApiExceptionHandler {
    private final MessageSource messageSource;

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ProductApiErrorResponse handleConstraintViolation(ConstraintViolationException e, ServletWebRequest request) {
        return ProductApiErrorResponse.builder()
                .timestamp(now().toEpochMilli())
                .path(request.getRequest().getRequestURI())
                .status(BAD_REQUEST.value())
                .error(BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .errors(e.getConstraintViolations().stream().map(cv -> ProductApiErrorResponse.ProductApiError.builder()
                        .defaultMessage(messageSource.getMessage(
                                cv.getMessageTemplate(),
                                new Object[] { cv.getLeafBean().getClass().getSimpleName(), cv.getPropertyPath().toString(), cv.getInvalidValue() },
                                cv.getMessage(),
                                request.getLocale()))
                        .objectName(decapitalize(cv.getRootBean().getClass().getSimpleName()))
                        .field(valueOf(cv.getPropertyPath()))
                        .rejectedValue(cv.getInvalidValue())
                        .code(cv.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                        .build()))
                .build();
    }
}
