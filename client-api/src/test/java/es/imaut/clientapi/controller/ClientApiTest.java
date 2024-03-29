package es.imaut.clientapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.imaut.clientapi.RandomClientExtension;
import es.imaut.clientapi.domain.ClientResponse;
import es.imaut.clientapi.domain.CreateClientRequest;
import io.github.glytching.junit.extension.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
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
@ExtendWith({ RandomClientExtension.class })
@ActiveProfiles("test")
class ClientApiTest {
    private static final Function<Integer, String> url = port -> "http://localhost:" + port;
    @LocalServerPort
    private int port;
    @Autowired
    private WebTestClient webClient;
    private final Supplier<String> clientsUrl = () -> url.apply(port) + "/clients";
    private final Supplier<String> clientsIdUrl = () -> url.apply(port) + "/clients/%d";

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
                        new ClientResponse(1L, "Client 1", "1234567890", "1 Main street", "PC01", "Capital", "Abroad"),
                        new ClientResponse(2L, "Client 2", "2345678901", "2 Main street", "PC02", "Village", "Inland"),
                        new ClientResponse(3L, "Client 3", "3456789012", "3 Main street", "PC03", "Town", "Overseas")
                );
    }

    @Test
    @DisplayName("IT: GET /clients/{id} should return 404 Not found")
    void getClientsIdShouldReturn404NotFound() {
        webClient.get().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: GET /clients/{id} should return 200 OK")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getClientsIdShouldReturn200Ok.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    })
    void getClientsIdShouldReturn200Ok() {
        webClient.get().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: GET /clients/{id} should return client from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getClientsIdShouldReturnClientFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    })
    void getClientsIdShouldReturnClientFromDatabase() {
        var response = webClient.get().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectBody(ClientResponse.class)
                .returnResult().getResponseBody();
        assertThat(response)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Client 1");
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
                            },
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "vatNumber",
                              "objectName": "createClientRequest"
                            },
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "streetAddress",
                              "objectName": "createClientRequest"
                            },
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "postcode",
                              "objectName": "createClientRequest"
                            },
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "city",
                              "objectName": "createClientRequest"
                            },
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "country",
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
                          "vatNumber": "1234567890",
                          "streetAddress": "1 Main street",
                          "postcode": "PC01",
                          "city": "Capital",
                          "country": "Abroad",
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
    @DisplayName("IT: PATCH /clients/{id} should return 404 Not found")
    void patchClientsIdShouldReturn404NotFound() {
        webClient.patch().uri(clientsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: PATCH /clients/{id} should return 400 Bad Request")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchClientsIdShouldReturn400BadRequest.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    })
    void patchClientsIdShouldReturn400BadRequest() {
        webClient.patch().uri(clientsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": null
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().json("""
                        {
                          "errors": [
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "name",
                              "objectName": "client"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @DisplayName("IT: PATCH /clients/{id} should return 200 OK")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchClientsIdShouldReturn200Ok.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    })
    void patchClientsIdShouldReturn200Ok() {
        webClient.patch().uri(clientsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: PATCH /clients/{id} should ignore unknown fields")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchClientsIdShouldIgnoreUnknownFields.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    })
    void patchClientsIdShouldIgnoreUnknownFields() {
        webClient.patch().uri(clientsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "ignored": "field"
                        }
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: PATCH /clients/{id} should return client from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchClientsIdShouldReturnClientFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    })
    void patchClientsIdShouldReturnClientFromDatabase() {
        var response = webClient.patch().uri(clientsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Client"
                        }
                        """)
                .exchange()
                .expectBody(ClientResponse.class).returnResult().getResponseBody();
        assertThat(response).hasFieldOrPropertyWithValue("name", "Updated Client");
    }

    @Test
    @DisplayName("IT: DELETE /clients/{id} should return 200 OK")
    void deleteClientsIdShouldReturn200Ok() {
        webClient.delete().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: DELETE /clients/{id} should delete client from database")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"deleteClientsIdShouldDeleteClientFromDatabase.sql"})
    void deleteClientsIdShouldDeleteClientFromDatabase() {
        webClient.get().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.delete().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.get().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
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

    @Test
    @DisplayName("IT: GET /clients/{id} should return client created from POST /clients")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    void getClientsIdShouldReturnClientCreatedFromPostClients(@Random CreateClientRequest request) {
        var client = webClient.post().uri(clientsUrl.get()).bodyValue(request)
                .exchange().expectBody(ClientResponse.class).returnResult().getResponseBody();
        var response = webClient.get().uri(clientsIdUrl.get().formatted(client.getId()))
                .exchange().expectBody(ClientResponse.class).returnResult().getResponseBody();
        assertThat(response).isEqualTo(client);
    }

    @Test
    @DisplayName("IT: GET /clients/{id} should return client updated from PATCH /clients/{id}")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getClientsIdShouldReturnClientUpdatedFromPatchClientsId.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    })
    void getClientsIdShouldReturnClientUpdatedFromPatchClientsId() {
        webClient.patch().uri(clientsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Client"
                        }
                        """)
                .exchange().expectStatus().isOk();
        var response = webClient.get().uri(clientsIdUrl.get().formatted(1L))
                .exchange().expectBody(ClientResponse.class).returnResult().getResponseBody();
        assertThat(response).hasFieldOrPropertyWithValue("name", "Updated Client");
    }

    @Test
    @DisplayName("IT: GET /clients/{id} should return 404 Not found after DELETE /clients/{id}")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getClientsIdShouldReturn404NotFoundAfterDeleteClientsId.sql"})
    void getClientsIdShouldReturn404NotFoundAfterDeleteClientsId() {
        webClient.delete().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.get().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: PATCH /clients/{id} should return client after POST /clients")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanClientTable.sql"})
    void patchClientsIdShouldReturnClientAfterPostClients(@Random CreateClientRequest request) {
        var client = webClient.post().uri(clientsUrl.get()).bodyValue(request)
                .exchange().expectBody(ClientResponse.class).returnResult().getResponseBody();
        var response = webClient.patch().uri(clientsIdUrl.get().formatted(client.getId()))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Client"
                        }
                        """)
                .exchange().expectBody(ClientResponse.class).returnResult().getResponseBody();
        assertThat(response)
                .hasFieldOrPropertyWithValue("id", client.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Client");
    }

    @Test
    @DisplayName("IT: PATCH /clients/{id} should return 404 Not found after DELETE /clients/{id}")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchClientsIdShouldReturn404NotFoundAfterDeleteClientsId.sql"})
    void patchClientsIdShouldReturn404NotFoundAfterDeleteClientsId() {
        webClient.delete().uri(clientsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.patch().uri(clientsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();
    }
}
