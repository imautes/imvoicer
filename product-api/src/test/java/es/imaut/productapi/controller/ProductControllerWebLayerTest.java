package es.imaut.productapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.productapi.RandomProductExtension;
import es.imaut.productapi.domain.ProductResponse;
import es.imaut.productapi.domain.CreateProductRequest;
import es.imaut.productapi.exception.ProductNotFoundException;
import es.imaut.productapi.service.ProductService;
import io.github.glytching.junit.extension.random.Random;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.json.JsonMergePatch;
import java.util.List;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { ProductController.class })
@ExtendWith({ RandomProductExtension.class })
class ProductControllerWebLayerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ProductService service;

    @Test
    @DisplayName("GET /products should return 200 OK")
    void getProductsShouldReturn200Ok() throws Exception {
        mvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /products should return products from service")
    void getProductsShouldReturnProductsFromService(@Random(type = ProductResponse.class) List<ProductResponse> products) throws Exception {
        when(service.findAll()).thenReturn(products);
        var result = mvc.perform(get("/products")).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(products));
    }

    @Test
    @DisplayName("GET /products/{id} should return 404 Not found")
    void getProductsIdShouldReturn404NotFound() throws Exception {
        when(service.findById(1L)).thenThrow(new ProductNotFoundException());
        mvc.perform(get("/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /products/{id} should return 200 OK")
    void getProductsIdShouldReturn200Ok(@Random ProductResponse response) throws Exception {
        when(service.findById(response.getId())).thenReturn(response);
        mvc.perform(get("/products/" + response.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /products/{id} should return product from service")
    void getProductsIdShouldReturnProductFromService(@Random ProductResponse response) throws Exception {
        when(service.findById(response.getId())).thenReturn(response);
        var result = mvc.perform(get("/products/" + response.getId())).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("POST /products should return 200 OK")
    void postProductsShouldReturn200Ok(@Random CreateProductRequest body) throws Exception {
        var request = post("/products")
                .contentType("application/json")
                .content(mapper.writeValueAsString(body));
        mvc.perform(request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /products should return 400 Bad Request")
    void postProductsShouldReturn400BadRequest() throws Exception {
        var request = post("/products")
                .contentType("application/json")
                .content(mapper.writeValueAsString(new CreateProductRequest()));
        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /products should ignore unknown fields")
    void postProductsShouldIgnoreUnknownFields() throws Exception {
        var body = """
                {
                  "name": "IT Consutancy",
                  "description": "Software development",
                  "netPrice": "350",
                  "currency": "EUR",
                  "unit": "day"
                }""";
        var request = post("/products")
                .contentType("application/json")
                .content(body);
        mvc.perform(request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /products should return product from service")
    void postProductsShouldReturnProductFromService(@Random CreateProductRequest body, @Random ProductResponse response) throws Exception {
        response.setName(body.getName());
        when(service.create(body)).thenReturn(response);
        var request = post("/products")
                .contentType("application/json")
                .content(mapper.writeValueAsString(body));
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("PATCH /products/{id} should return 404 Not found")
    void patchProductsIdShouldReturn404NotFound() throws Exception {
        when(service.update(anyLong(), any(JsonMergePatch.class))).thenThrow(new ProductNotFoundException());
        var request = patch("/products/1")
                .contentType("application/merge-patch+json")
                .content("{}");
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /products/{id} should return 400 Bad Request")
    void patchProductsIdShouldReturn400BadRequest() throws Exception {
        when(service.update(anyLong(), any(JsonMergePatch.class))).thenThrow(new ConstraintViolationException(emptySet()));
        var request = patch("/products/1")
                .contentType("application/merge-patch+json")
                .content("""
                        {
                          "name": ""
                        }
                        """);
        mvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /products/{id} should return 200 OK")
    void patchProductsIdShouldReturn200Ok(@Random ProductResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/products/" + response.getId())
                .contentType("application/merge-patch+json")
                .content("{}");
        mvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /products/{id} should ignore unknown fields")
    void patchProductsIdShouldIgnoreUnknownFields(@Random ProductResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/products/" + response.getId())
                .contentType("application/merge-patch+json")
                .content("""
                        {
                          "ignored": "field"
                        }
                        """);
        mvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /products/{id} should return product from service")
    void patchProductsIdShouldReturnProductFromService(@Random ProductResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/products/" + response.getId())
                .contentType("application/merge-patch+json")
                .content("{}");
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("DELETE /products/{id} should return 200 OK")
    void deleteProductsIdShouldReturn200Ok() throws Exception {
        mvc.perform(delete("/products/1"))
                .andExpect(status().isOk());
    }
}
