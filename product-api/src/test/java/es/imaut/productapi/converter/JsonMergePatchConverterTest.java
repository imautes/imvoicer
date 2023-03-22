package es.imaut.productapi.converter;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import javax.json.JsonMergePatch;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static javax.json.Json.createMergePatch;
import static javax.json.Json.createObjectBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class JsonMergePatchConverterTest {
    private static final JsonMergePatchConverter converter = new JsonMergePatchConverter();

    @Test
    @DisplayName("Constructor should set supported media type")
    void constructorShouldSetSupportedMediaType() {
        assertThat(converter.getSupportedMediaTypes())
                .asList().hasSize(1)
                .containsExactly(MediaType.valueOf("application/merge-patch+json"));
    }

    @Test
    @DisplayName("Supports should return true")
    void supportsShouldReturnTrue() {
        assertThat(converter.supports(JsonMergePatch.class))
                .isTrue();
    }

    @Test
    @DisplayName("Supports should return false")
    void supportsShouldReturnFalse() {
        assertThat(converter.supports(Object.class))
                .isFalse();
    }

    @Test
    @DisplayName("Read internal should return merge patch")
    void readInternalShouldReturnMergePatch() {
        assertThat(converter.readInternal(JsonMergePatch.class, new MockHttpInputMessage("{}")))
                .isNotNull();
    }

    @Test
    @DisplayName("Read internal should throw HTTP message not readable exception")
    void readInternalShouldThrowHttpMessageNotReadableException() {
        assertThatThrownBy(() -> converter.readInternal(JsonMergePatch.class, new MockHttpInputMessage("")))
                .isInstanceOf(HttpMessageNotReadableException.class);
    }

    @Test
    @DisplayName("Write internal should write merge patch")
    void writeInternalShouldWriteMergePatch() {
        var httpOutput = new MockHttpOutputMessage();
        converter.writeInternal(createMergePatch(createObjectBuilder().build()), httpOutput);
        assertThat(httpOutput.getBody().toString()).isEmpty();
    }

    @Test
    @DisplayName("Write internal should throw HTTP message not writable exception")
    void writeInternalShouldThrowHttpMessageNotWritableException() {
        assertThatThrownBy(() -> converter.writeInternal(null, new MockHttpOutputMessage()))
                .isInstanceOf(HttpMessageNotWritableException.class);
    }

    @RequiredArgsConstructor
    private static final class MockHttpInputMessage implements HttpInputMessage {
        private final String message;

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(message.getBytes());
        }

        @Override
        public HttpHeaders getHeaders() {
            return null;
        }
    }

    @RequiredArgsConstructor
    private static final class MockHttpOutputMessage implements HttpOutputMessage {
        @Override
        public OutputStream getBody() {
            return new ByteArrayOutputStream();
        }

        @Override
        public HttpHeaders getHeaders() {
            return null;
        }
    }
}
