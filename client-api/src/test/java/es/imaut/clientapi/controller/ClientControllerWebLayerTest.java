package es.imaut.clientapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.clientapi.domain.ClientResponse;
import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.exception.ClientNotFoundException;
import es.imaut.clientapi.service.ClientService;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
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

@WebMvcTest(controllers = { ClientController.class })
@ExtendWith({ RandomBeansExtension.class })
class ClientControllerWebLayerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ClientService service;

    @Test
    @DisplayName("GET /clients should return 200 OK")
    void getClientsShouldReturn200Ok() throws Exception {
        mvc.perform(get("/clients"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /clients should return clients from service")
    void getClientsShouldReturnClientsFromService(@Random(type = ClientResponse.class) List<ClientResponse> clients) throws Exception {
        when(service.findAll()).thenReturn(clients);
        var result = mvc.perform(get("/clients")).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(clients));
    }

    @Test
    @DisplayName("GET /clients/{id} should return 404 Not found")
    void getClientsIdShouldReturn404NotFound() throws Exception {
        when(service.findById(1L)).thenThrow(new ClientNotFoundException());
        mvc.perform(get("/clients/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /clients/{id} should return 200 OK")
    void getClientsIdShouldReturn200Ok(@Random ClientResponse response) throws Exception {
        when(service.findById(response.getId())).thenReturn(response);
        mvc.perform(get("/clients/" + response.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /clients/{id} should return client from service")
    void getClientsIdShouldReturnClientFromService(@Random ClientResponse response) throws Exception {
        when(service.findById(response.getId())).thenReturn(response);
        var result = mvc.perform(get("/clients/" + response.getId())).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("POST /clients should return 200 OK")
    void postClientsShouldReturn200Ok(@Random CreateClientRequest body) throws Exception {
        var request = post("/clients")
                .contentType("application/json")
                .content(mapper.writeValueAsString(body));
        mvc.perform(request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /clients should return 400 Bad Request")
    void postClientsShouldReturn400BadRequest() throws Exception {
        var request = post("/clients")
                .contentType("application/json")
                .content(mapper.writeValueAsString(new CreateClientRequest()));
        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /clients should ignore unknown fields")
    void postClientsShouldIgnoreUnknownFields() throws Exception {
        var body = """
                {
                  "name": "Name",
                  "ignored": "field"
                }""";
        var request = post("/clients")
                .contentType("application/json")
                .content(body);
        mvc.perform(request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /clients should return client from service")
    void postClientsShouldReturnClientFromService(@Random CreateClientRequest body, @Random ClientResponse response) throws Exception {
        response.setName(body.getName());
        when(service.create(body)).thenReturn(response);
        var request = post("/clients")
                .contentType("application/json")
                .content(mapper.writeValueAsString(body));
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("PATCH /clients/{id} should return 404 Not found")
    void patchClientsIdShouldReturn404NotFound() throws Exception {
        when(service.update(anyLong(), any(JsonMergePatch.class))).thenThrow(new ClientNotFoundException());
        var request = patch("/clients/1")
                .contentType("application/merge-patch+json")
                .content("{}");
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /clients/{id} should return 400 Bad Request")
    void patchClientsIdShouldReturn400BadRequest() throws Exception {
        when(service.update(anyLong(), any(JsonMergePatch.class))).thenThrow(new ConstraintViolationException(emptySet()));
        var request = patch("/clients/1")
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
    @DisplayName("PATCH /clients/{id} should return 200 OK")
    void patchClientsIdShouldReturn200Ok(@Random ClientResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/clients/" + response.getId())
                .contentType("application/merge-patch+json")
                .content("{}");
        mvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /clients/{id} should ignore unknown fields")
    void patchClientsIdShouldIgnoreUnknownFields(@Random ClientResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/clients/" + response.getId())
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
    @DisplayName("PATCH /clients/{id} should return client from service")
    void patchClientsIdShouldReturnClientFromService(@Random ClientResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/clients/" + response.getId())
                .contentType("application/merge-patch+json")
                .content("{}");
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("DELETE /clients/{id} should return 200 OK")
    void deleteClientsIdShouldReturn200Ok() throws Exception {
        mvc.perform(delete("/clients/1"))
                .andExpect(status().isOk());
    }
}
