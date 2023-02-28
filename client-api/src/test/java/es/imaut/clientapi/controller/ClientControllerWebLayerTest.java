package es.imaut.clientapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.clientapi.domain.ClientDetails;
import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.service.ClientService;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void getClientsShouldReturnClientsFromService(@Random(type = ClientDetails.class) List<ClientDetails> clients) throws Exception {
        when(service.findAll()).thenReturn(clients);
        var result = mvc.perform(get("/clients")).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(clients));
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
    void postClientsShouldReturnClientFromService(@Random CreateClientRequest body, @Random ClientDetails details) throws Exception {
        details.setName(body.getName());
        when(service.create(body)).thenReturn(details);
        var request = post("/clients")
                .contentType("application/json")
                .content(mapper.writeValueAsString(body));
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(details));
    }
}
