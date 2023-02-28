package es.imaut.clientapi.controller;

import es.imaut.clientapi.dto.ClientDto;
import es.imaut.clientapi.service.ClientService;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith({ MockitoExtension.class, RandomBeansExtension.class })
class ClientControllerTest {
    @Mock
    private ClientService service;
    @InjectMocks
    private ClientController controller;

    @Test
    @DisplayName("Find all should return 200 OK")
    void findAllShouldReturn200Ok() {
        var result = controller.findAll();
        assertThat(result).hasFieldOrPropertyWithValue("status", OK);
    }

    @Test
    @DisplayName("Find all should return clients from service")
    void findAllShouldReturnClientsFromService(@Random(type = ClientDto.class)List<ClientDto> clients) {
        when(service.findAll()).thenReturn(clients);
        var result = controller.findAll();
        assertThat(result.getBody()).asList().hasSameSizeAs(clients)
                .usingRecursiveFieldByFieldElementComparator().isEqualTo(clients);
    }
}
