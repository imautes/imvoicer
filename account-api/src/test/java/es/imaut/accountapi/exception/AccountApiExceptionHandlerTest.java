package es.imaut.accountapi.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Locale;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith({MockitoExtension.class})
class AccountApiExceptionHandlerTest {
    @Mock
    private MessageSource messageSource;
    @InjectMocks
    private AccountApiExceptionHandler handler;

    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        try (var factory = buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("CVE response should contain timestamp")
    void cveResponseShouldContainTimestamp() {
        var ex = new ConstraintViolationException(emptySet());
        var req = new ServletWebRequest(new MockHttpServletRequest());
        var result = handler.handleConstraintViolation(ex, req);
        assertThat(result.timestamp()).isNotZero();
    }

    @Test
    @DisplayName("CVE response should contain path")
    void cveResponseShouldContainPath() {
        var path = "/path";
        var ex = new ConstraintViolationException(emptySet());
        var req = new ServletWebRequest(new MockHttpServletRequest("GET", path));
        var result = handler.handleConstraintViolation(ex, req);
        assertThat(result.path()).isEqualTo(path);
    }

    @Test
    @DisplayName("CVE response should contain status")
    void cveResponseShouldContainStatus() {
        var ex = new ConstraintViolationException(emptySet());
        var req = new ServletWebRequest(new MockHttpServletRequest());
        var result = handler.handleConstraintViolation(ex, req);
        assertThat(result.status()).isEqualTo(BAD_REQUEST.value());
    }

    @Test
    @DisplayName("CVE response should contain error")
    void cveResponseShouldContainError() {
        var ex = new ConstraintViolationException(emptySet());
        var req = new ServletWebRequest(new MockHttpServletRequest());
        var result = handler.handleConstraintViolation(ex, req);
        assertThat(result.error()).isEqualTo(BAD_REQUEST.getReasonPhrase());
    }

    @Test
    @DisplayName("CVE response should contain message")
    void cveResponseShouldContainMessage() {
        var ex = new ConstraintViolationException("Exception message", emptySet());
        var req = new ServletWebRequest(new MockHttpServletRequest());
        var result = handler.handleConstraintViolation(ex, req);
        assertThat(result.message()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("CVE response should contain errors")
    void cveResponseShouldContainErrors() {
        var violations = validator.validate(new TestBean());
        var ex = new ConstraintViolationException(violations);
        var req = new ServletWebRequest(new MockHttpServletRequest());
        var result = handler.handleConstraintViolation(ex, req);
        assertThat(result.errors().toList()).asList()
                .isNotEmpty()
                .hasSameSizeAs(violations);
    }

    @Test
    @DisplayName("CVE response should contain errors' default message")
    void cveResponseShouldContainErrorsDefaultMessage() {
        var defaultMessage = "Default message";
        when(messageSource.getMessage(anyString(), any(Object[].class), anyString(), any(Locale.class)))
                .thenReturn(defaultMessage);
        var req = new ServletWebRequest(new MockHttpServletRequest());
        var violations = validator.validate(new TestBean());
        var ex = new ConstraintViolationException(violations);
        var result = handler.handleConstraintViolation(ex, req);
        assertThat(result.errors().toList().get(0))
                .hasFieldOrPropertyWithValue("defaultMessage", defaultMessage);
    }

    @ParameterizedTest
    @DisplayName("CVE response should contain errors' data")
    @CsvSource({
            "objectName,testBean",
            "field,field",
            "rejectedValue,",
            "code,NotNull"
    })
    void cveResponseShouldContainErrorsObjectName(String property, String value) {
        var req = new ServletWebRequest(new MockHttpServletRequest());
        var violations = validator.validate(new TestBean());
        var ex = new ConstraintViolationException(violations);
        var result = handler.handleConstraintViolation(ex, req);
        assertThat(result.errors().toList().get(0))
                .hasFieldOrPropertyWithValue(property, value);
    }

    @Data
    private static final class TestBean {
        @NotNull
        private String field;
    }
}
