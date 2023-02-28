package es.imaut.clientapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.clientapi.model.Client;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@ExtendWith({ RandomBeansExtension.class })
class ClientApiTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("IT: GET /clients should return 200 OK")
    void getClientsShouldReturn200Ok() throws Exception {
        mvc.perform(get("/clients").content("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT: GET /clients should return empty list")
    void getClientsShouldReturnEmptyList() throws Exception {
        mvc.perform(get("/clients").content("application/json"))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("IT: GET /clients should return clients from database")
    @Transactional
    void getClientsShouldReturnEmptyList(@Random(type = Client.class) List<Client> clients) throws Exception {
        clients.forEach(entityManager::persist);
        mvc.perform(get("/clients").content("application/json"))
                .andExpect(content().json(mapper.writeValueAsString(clients)));
    }
}
