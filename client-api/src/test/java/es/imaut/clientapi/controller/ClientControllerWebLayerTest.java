package es.imaut.clientapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.clientapi.dto.ClientDto;
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
        mvc.perform(get("/clients").content("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /clients should return clients from service")
    void getClientsShouldReturnClientsFromService(@Random(type = ClientDto.class) List<ClientDto> clients) throws Exception {
        when(service.findAll()).thenReturn(clients);
        var result = mvc.perform(get("/clients").content("application/json")).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(clients));
    }
}
