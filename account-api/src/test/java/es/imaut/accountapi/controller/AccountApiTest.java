package es.imaut.accountapi.controller;

import es.imaut.accountapi.RandomAccountExtension;
import es.imaut.accountapi.domain.AccountResponse;
import es.imaut.accountapi.domain.CreateAccountRequest;
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

import static java.util.Collections.emptySet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith({ RandomAccountExtension.class })
@ActiveProfiles("test")
class AccountApiTest {
    private static final Function<Integer, String> url = port -> "http://localhost:" + port;
    @LocalServerPort
    private int port;
    @Autowired
    private WebTestClient webClient;
    private final Supplier<String> accountsUrl = () -> url.apply(port) + "/accounts";
    private final Supplier<String> accountsIdUrl = () -> url.apply(port) + "/accounts/%d";

    @Test
    @DisplayName("IT: GET /accounts should return 200 OK")
    void getAccountsShouldReturn200Ok() {
        webClient.get().uri(accountsUrl.get()).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: GET /accounts should return empty list")
    void getAccountsShouldReturnEmptyList() {
        webClient.get().uri(accountsUrl.get()).exchange()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json("[]");
    }

    @Test
    @DisplayName("IT: GET /accounts should return accounts from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getAccountsShouldReturnAccountsFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    })
    void getAccountsShouldReturnAccountsFromDatabase() {
        webClient.get().uri(accountsUrl.get()).exchange()
                .expectBodyList(AccountResponse.class)
                .hasSize(3)
                .contains(
                        new AccountResponse(1L, "John Doe", null, null, "Self Employed", null, emptySet()),
                        new AccountResponse(2L, "Jane Doe", null, null, "Self Employed", null, emptySet()),
                        new AccountResponse(3L, "Does Company", null, null, "Ltd", null, emptySet())
                );
    }

    @Test
    @DisplayName("IT: GET /accounts/{id} should return 404 Not found")
    void getAccountsIdShouldReturn404NotFound() {
        webClient.get().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: GET /accounts/{id} should return 200 OK")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getAccountsIdShouldReturn200Ok.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    })
    void getAccountsIdShouldReturn200Ok() {
        webClient.get().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: GET /accounts/{id} should return account from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getAccountsIdShouldReturnAccountFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    })
    void getAccountsIdShouldReturnAccountFromDatabase() {
        var response = webClient.get().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectBody(AccountResponse.class)
                .returnResult().getResponseBody();
        assertThat(response)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "John Doe");
    }

    @Test
    @DisplayName("IT: POST /accounts should return 200 OK")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    void postAccountsShouldReturn200Ok(@Random CreateAccountRequest request) {
        webClient.post().uri(accountsUrl.get()).bodyValue(request).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: POST /accounts should return 400 Bad Request")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    void postAccountsShouldReturn400BadRequest() {
        webClient.post().uri(accountsUrl.get()).bodyValue(new CreateAccountRequest()).exchange()
                .expectStatus().isBadRequest()
                .expectBody().json("""
                        {
                          "errors": [
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "name",
                              "objectName": "createAccountRequest"
                            },
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "type",
                              "objectName": "createAccountRequest"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @DisplayName("IT: POST /accounts should ignore unknown fields")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    void postAccountsShouldIgnoreUnknownFields() {
        webClient.post().uri(accountsUrl.get()).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "name": "John Doe",
                          "type": "Self Employed"
                        }
                        """).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: POST /accounts should return account from database")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    void postAccountsShouldReturnAccountFromDatabase(@Random CreateAccountRequest request) {
        var response = webClient.post().uri(accountsUrl.get()).bodyValue(request).exchange()
                .expectBody(AccountResponse.class)
                .returnResult().getResponseBody();
        assertThat(response).hasFieldOrPropertyWithValue("name", request.getName());
    }

    @Test
    @DisplayName("IT: PATCH /accounts/{id} should return 404 Not found")
    void patchAccountsIdShouldReturn404NotFound() {
        webClient.patch().uri(accountsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: PATCH /accounts/{id} should return 400 Bad Request")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchAccountsIdShouldReturn400BadRequest.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    })
    void patchAccountsIdShouldReturn400BadRequest() {
        webClient.patch().uri(accountsIdUrl.get().formatted(1L))
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
                              "objectName": "account"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @DisplayName("IT: PATCH /accounts/{id} should return 200 OK")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchAccountsIdShouldReturn200Ok.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    })
    void patchAccountsIdShouldReturn200Ok() {
        webClient.patch().uri(accountsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: PATCH /accounts/{id} should ignore unknown fields")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchAccountsIdShouldIgnoreUnknownFields.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    })
    void patchAccountsIdShouldIgnoreUnknownFields() {
        webClient.patch().uri(accountsIdUrl.get().formatted(1L))
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
    @DisplayName("IT: PATCH /accounts/{id} should return account from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchAccountsIdShouldReturnAccountFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    })
    void patchAccountsIdShouldReturnAccountFromDatabase() {
        var response = webClient.patch().uri(accountsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Account"
                        }
                        """)
                .exchange()
                .expectBody(AccountResponse.class).returnResult().getResponseBody();
        assertThat(response).hasFieldOrPropertyWithValue("name", "Updated Account");
    }

    @Test
    @DisplayName("IT: DELETE /accounts/{id} should return 200 OK")
    void deleteAccountsIdShouldReturn200Ok() {
        webClient.delete().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: DELETE /accounts/{id} should delete account from database")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"deleteAccountsIdShouldDeleteAccountFromDatabase.sql"})
    void deleteAccountsIdShouldDeleteAccountFromDatabase() {
        webClient.get().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.delete().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.get().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: GET /accounts should return account created from POST /accounts")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    void getAccountsShouldReturnAccountCreatedFromPostAccounts(@Random CreateAccountRequest request) {
        webClient.post().uri(accountsUrl.get()).bodyValue(request).exchange();
        var response = webClient.get().uri(accountsUrl.get()).exchange()
                .expectBodyList(AccountResponse.class)
                .hasSize(1)
                .returnResult().getResponseBody();
        assertThat(response).asList()
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "accountDetailsClientId", "bankDetails")
                .isEqualTo(List.of(request));
    }

    @Test
    @DisplayName("IT: GET /accounts/{id} should return account created from POST /accounts")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    void getAccountsIdShouldReturnAccountCreatedFromPostAccounts(@Random CreateAccountRequest request) {
        var account = webClient.post().uri(accountsUrl.get()).bodyValue(request)
                .exchange().expectBody(AccountResponse.class).returnResult().getResponseBody();
        var response = webClient.get().uri(accountsIdUrl.get().formatted(account.getId()))
                .exchange().expectBody(AccountResponse.class).returnResult().getResponseBody();
        assertThat(response).usingRecursiveComparison().ignoringFields("bankDetails").isEqualTo(account);
    }

    @Test
    @DisplayName("IT: GET /accounts/{id} should return account updated from PATCH /accounts/{id}")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getAccountsIdShouldReturnAccountUpdatedFromPatchAccountsId.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    })
    void getAccountsIdShouldReturnAccountUpdatedFromPatchAccountsId() {
        webClient.patch().uri(accountsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Account"
                        }
                        """)
                .exchange().expectStatus().isOk();
        var response = webClient.get().uri(accountsIdUrl.get().formatted(1L))
                .exchange().expectBody(AccountResponse.class).returnResult().getResponseBody();
        assertThat(response).hasFieldOrPropertyWithValue("name", "Updated Account");
    }

    @Test
    @DisplayName("IT: GET /accounts/{id} should return 404 Not found after DELETE /accounts/{id}")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getAccountsIdShouldReturn404NotFoundAfterDeleteAccountsId.sql"})
    void getAccountsIdShouldReturn404NotFoundAfterDeleteAccountsId() {
        webClient.delete().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.get().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: PATCH /accounts/{id} should return account after POST /accounts")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanAccountTable.sql"})
    void patchAccountsIdShouldReturnAccountAfterPostAccounts(@Random CreateAccountRequest request) {
        var account = webClient.post().uri(accountsUrl.get()).bodyValue(request)
                .exchange().expectBody(AccountResponse.class).returnResult().getResponseBody();
        var response = webClient.patch().uri(accountsIdUrl.get().formatted(account.getId()))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Account"
                        }
                        """)
                .exchange().expectBody(AccountResponse.class).returnResult().getResponseBody();
        assertThat(response)
                .hasFieldOrPropertyWithValue("id", account.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Account");
    }

    @Test
    @DisplayName("IT: PATCH /accounts/{id} should return 404 Not found after DELETE /accounts/{id}")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchAccountsIdShouldReturn404NotFoundAfterDeleteAccountsId.sql"})
    void patchAccountsIdShouldReturn404NotFoundAfterDeleteAccountsId() {
        webClient.delete().uri(accountsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.patch().uri(accountsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();
    }
}
