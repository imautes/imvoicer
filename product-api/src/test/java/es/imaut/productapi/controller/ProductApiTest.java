package es.imaut.productapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.imaut.productapi.RandomProductExtension;
import es.imaut.productapi.domain.ProductResponse;
import es.imaut.productapi.domain.CreateProductRequest;
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

import java.math.BigDecimal;
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
@ExtendWith({ RandomProductExtension.class })
@ActiveProfiles("test")
class ProductApiTest {
    private static final Function<Integer, String> url = port -> "http://localhost:" + port;
    @LocalServerPort
    private int port;
    @Autowired
    private WebTestClient webClient;
    private final Supplier<String> productsUrl = () -> url.apply(port) + "/products";
    private final Supplier<String> productsIdUrl = () -> url.apply(port) + "/products/%d";

    @Test
    @DisplayName("IT: GET /products should return 200 OK")
    void getProductsShouldReturn200Ok() {
        webClient.get().uri(productsUrl.get()).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: GET /products should return empty list")
    void getProductsShouldReturnEmptyList() {
        webClient.get().uri(productsUrl.get()).exchange()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json("[]");
    }

    @Test
    @DisplayName("IT: GET /products should return products from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getProductsShouldReturnProductsFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    })
    void getProductsShouldReturnProductsFromDatabase() throws JsonProcessingException {
        webClient.get().uri(productsUrl.get()).exchange()
                .expectBodyList(ProductResponse.class)
                .hasSize(3)
                .contains(
                        new ProductResponse(1L, "Europe IT Consultancy", "Software development", new BigDecimal("350.0000"), "EUR", "day"),
                        new ProductResponse(2L, "UK IT Consultancy", "Software development", new BigDecimal("50.0000"), "GBP", "hour"),
                        new ProductResponse(3L, "USA IT Consultancy", "Software development", new BigDecimal("2000.0000"), "USD", "week")
                );
    }

    @Test
    @DisplayName("IT: GET /products/{id} should return 404 Not found")
    void getProductsIdShouldReturn404NotFound() {
        webClient.get().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: GET /products/{id} should return 200 OK")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getProductsIdShouldReturn200Ok.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    })
    void getProductsIdShouldReturn200Ok() {
        webClient.get().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: GET /products/{id} should return product from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getProductsIdShouldReturnProductFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    })
    void getProductsIdShouldReturnProductFromDatabase() {
        var response = webClient.get().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectBody(ProductResponse.class)
                .returnResult().getResponseBody();
        assertThat(response)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "IT Consultancy");
    }

    @Test
    @DisplayName("IT: POST /products should return 200 OK")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    void postProductsShouldReturn200Ok(@Random CreateProductRequest request) {
        webClient.post().uri(productsUrl.get()).bodyValue(request).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: POST /products should return 400 Bad Request")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    void postProductsShouldReturn400BadRequest() {
        webClient.post().uri(productsUrl.get()).bodyValue(new CreateProductRequest()).exchange()
                .expectStatus().isBadRequest()
                .expectBody().json("""
                        {
                          "errors": [
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "name",
                              "objectName": "createProductRequest"
                            },
                            {
                              "code": "NotNull",
                              "rejectedValue": null,
                              "field": "netPrice",
                              "objectName": "createProductRequest"
                            },
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "currency",
                              "objectName": "createProductRequest"
                            },
                            {
                              "code": "NotBlank",
                              "rejectedValue": null,
                              "field": "unit",
                              "objectName": "createProductRequest"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @DisplayName("IT: POST /products should ignore unknown fields")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    void postProductsShouldIgnoreUnknownFields() {
        webClient.post().uri(productsUrl.get()).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "name": "IT Consultancy",
                          "description": "Software development",
                          "netPrice": "350",
                          "currency": "EUR",
                          "unit": "day"
                        }
                        """).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: POST /products should return product from database")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    void postProductsShouldReturnProductFromDatabase(@Random CreateProductRequest request) {
        var response = webClient.post().uri(productsUrl.get()).bodyValue(request).exchange()
                .expectBody(ProductResponse.class)
                .returnResult().getResponseBody();
        assertThat(response).hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("name", request.getName());
    }

    @Test
    @DisplayName("IT: PATCH /products/{id} should return 404 Not found")
    void patchProductsIdShouldReturn404NotFound() {
        webClient.patch().uri(productsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: PATCH /products/{id} should return 400 Bad Request")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchProductsIdShouldReturn400BadRequest.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    })
    void patchProductsIdShouldReturn400BadRequest() {
        webClient.patch().uri(productsIdUrl.get().formatted(1L))
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
                              "objectName": "product"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @DisplayName("IT: PATCH /products/{id} should return 200 OK")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchProductsIdShouldReturn200Ok.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    })
    void patchProductsIdShouldReturn200Ok() {
        webClient.patch().uri(productsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: PATCH /products/{id} should ignore unknown fields")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchProductsIdShouldIgnoreUnknownFields.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    })
    void patchProductsIdShouldIgnoreUnknownFields() {
        webClient.patch().uri(productsIdUrl.get().formatted(1L))
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
    @DisplayName("IT: PATCH /products/{id} should return product from database")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchProductsIdShouldReturnProductFromDatabase.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    })
    void patchProductsIdShouldReturnProductFromDatabase() {
        var response = webClient.patch().uri(productsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Product"
                        }
                        """)
                .exchange()
                .expectBody(ProductResponse.class).returnResult().getResponseBody();
        assertThat(response).hasFieldOrPropertyWithValue("name", "Updated Product");
    }

    @Test
    @DisplayName("IT: DELETE /products/{id} should return 200 OK")
    void deleteProductsIdShouldReturn200Ok() {
        webClient.delete().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("IT: DELETE /products/{id} should delete product from database")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"deleteProductsIdShouldDeleteProductFromDatabase.sql"})
    void deleteProductsIdShouldDeleteProductFromDatabase() {
        webClient.get().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.delete().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.get().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: GET /products should return product created from POST /products")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    void getProductsShouldReturnProductCreatedFromPostProducts(@Random CreateProductRequest request) {
        webClient.post().uri(productsUrl.get()).bodyValue(request).exchange();
        var response = webClient.get().uri(productsUrl.get()).exchange()
                .expectBodyList(ProductResponse.class)
                .hasSize(1)
                .returnResult().getResponseBody();
        assertThat(response).asList()
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(List.of(request));
    }

    @Test
    @DisplayName("IT: GET /products/{id} should return product created from POST /products")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    void getProductsIdShouldReturnProductCreatedFromPostProducts(@Random CreateProductRequest request) {
        var product = webClient.post().uri(productsUrl.get()).bodyValue(request)
                .exchange().expectBody(ProductResponse.class).returnResult().getResponseBody();
        var response = webClient.get().uri(productsIdUrl.get().formatted(product.getId()))
                .exchange().expectBody(ProductResponse.class).returnResult().getResponseBody();
        assertThat(response).isEqualTo(product);
    }

    @Test
    @DisplayName("IT: GET /products/{id} should return product updated from PATCH /products/{id}")
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getProductsIdShouldReturnProductUpdatedFromPatchProductsId.sql"}),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    })
    void getProductsIdShouldReturnProductUpdatedFromPatchProductsId() {
        webClient.patch().uri(productsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Product"
                        }
                        """)
                .exchange().expectStatus().isOk();
        var response = webClient.get().uri(productsIdUrl.get().formatted(1L))
                .exchange().expectBody(ProductResponse.class).returnResult().getResponseBody();
        assertThat(response).hasFieldOrPropertyWithValue("name", "Updated Product");
    }

    @Test
    @DisplayName("IT: GET /products/{id} should return 404 Not found after DELETE /products/{id}")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"getProductsIdShouldReturn404NotFoundAfterDeleteProductsId.sql"})
    void getProductsIdShouldReturn404NotFoundAfterDeleteProductsId() {
        webClient.delete().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.get().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("IT: PATCH /products/{id} should return product after POST /products")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"cleanProductTable.sql"})
    void patchProductsIdShouldReturnProductAfterPostProducts(@Random CreateProductRequest request) {
        var product = webClient.post().uri(productsUrl.get()).bodyValue(request)
                .exchange().expectBody(ProductResponse.class).returnResult().getResponseBody();
        var response = webClient.patch().uri(productsIdUrl.get().formatted(product.getId()))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("""
                        {
                          "name": "Updated Product"
                        }
                        """)
                .exchange().expectBody(ProductResponse.class).returnResult().getResponseBody();
        assertThat(response)
                .hasFieldOrPropertyWithValue("id", product.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Product");
    }

    @Test
    @DisplayName("IT: PATCH /products/{id} should return 404 Not found after DELETE /products/{id}")
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"patchProductsIdShouldReturn404NotFoundAfterDeleteProductsId.sql"})
    void patchProductsIdShouldReturn404NotFoundAfterDeleteProductsId() {
        webClient.delete().uri(productsIdUrl.get().formatted(1L)).exchange()
                .expectStatus().isOk();
        webClient.patch().uri(productsIdUrl.get().formatted(1L))
                .contentType(MediaType.valueOf("application/merge-patch+json"))
                .bodyValue("{}")
                .exchange()
                .expectStatus().isNotFound();
    }
}
