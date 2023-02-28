package es.imaut.clientapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.imaut.clientapi.domain.ClientResponse;
import es.imaut.clientapi.domain.CreateClientRequest;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith({ RandomBeansExtension.class })
@ActiveProfiles("test")
class ClientApiTest {
    private static final Function<Integer, String> url = port -> "http://localhost:" + port;
    @LocalServerPort
    private int port;
    @Autowired
    private WebTestClient webClient;
    private final Supplier<String> clientsUrl = () -> url.apply(port) + "/clients";

    @Test
    @DisplayName("IT: GET /clients should return 200 OK")
    void getClientsShouldReturn200Ok() {
        webClient.get().uri(clientsUrl.get()).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: GET /clients should return empty list")
    void getClientsShouldReturnEmptyList() {
        webClient.get().uri(clientsUrl.get()).exchange()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json("[]");
    }

    @Test
    @DisplayName("IT: GET /clients should return clients from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getClientsShouldReturnClientsFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    })
    void getClientsShouldReturnClientsFromDatabase() throws JsonProcessingException {
        webClient.get().uri(clientsUrl.get()).exchange()
                .expectBodyList(ClientResponse.class)
                .hasSize(3)
                .contains(
                        new ClientResponse(1L, "Client 1"),
                        new ClientResponse(2L, "Client 2"),
                        new ClientResponse(3L, "Client 3")
                );
    }

    @Test
    @DisplayName("IT: POST /clients should return 200 OK")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    void postClientsShouldReturn200Ok(@Random CreateClientRequest request) {
        webClient.post().uri(clientsUrl.get()).bodyValue(request).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: POST /clients should return 400 Bad Request")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    void postClientsShouldReturn400BadRequest() {
        webClient.post().uri(clientsUrl.get()).bodyValue(new CreateClientRequest()).exchange()
                .expectStatus().isBadRequest()
                .expectBody().json("""
                        {
                          "errors": [
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "name",
                              "objectName": "createClientRequest"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @DisplayName("IT: POST /clients should ignore unknown fields")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    void postClientsShouldIgnoreUnknownFields() {
        webClient.post().uri(clientsUrl.get()).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "name": "Client name",
                          "ignored": "field"
                        }
                        """).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: POST /clients should return client from database")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    void postClientsShouldReturnClientFromDatabase(@Random CreateClientRequest request) {
        var response = webClient.post().uri(clientsUrl.get()).bodyValue(request).exchange()
                .expectBody(ClientResponse.class)
                .returnResult().getResponseBody();
        assertThat(response).hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("name", request.getName());
    }

    @Test
    @DisplayName("IT: GET /clients should return client created from POST /clients")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    void getClientsShouldReturnClientCreatedFromPostClients(@Random CreateClientRequest request) {
        webClient.post().uri(clientsUrl.get()).bodyValue(request).exchange();
        var response = webClient.get().uri(clientsUrl.get()).exchange()
                .expectBodyList(ClientResponse.class)
                .hasSize(1)
                .returnResult().getResponseBody();
        assertThat(response).asList()
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(List.of(request));
    }
}
