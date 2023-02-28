package es.imaut.clientapi.controller;

import es.imaut.clientapi.domain.ClientResponse;
import es.imaut.clientapi.domain.CreateClientRequest;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith({ MockitoExtension.class, RandomBeansExtension.class })
class ClientControllerTest {
    @Mock
    private ClientService service;
    @InjectMocks
    private ClientController controller;

    @Test
    @DisplayName("Find all should call service")
    void findAllShouldCallService() {
        controller.findAll();
        verify(service).findAll();
    }

    @Test
    @DisplayName("Find all should return 200 OK")
    void findAllShouldReturn200Ok() {
        var result = controller.findAll();
        assertThat(result).hasFieldOrPropertyWithValue("status", OK);
    }

    @Test
    @DisplayName("Find all should return clients from service")
    void findAllShouldReturnClientsFromService(@Random(type = ClientResponse.class) List<ClientResponse> clients) {
        when(service.findAll()).thenReturn(clients);
        var result = controller.findAll();
        assertThat(result.getBody()).asList().hasSameSizeAs(clients)
                .usingRecursiveFieldByFieldElementComparator().isEqualTo(clients);
    }

    @Test
    @DisplayName("Create should call service")
    void createShouldCallService(@Random CreateClientRequest request) {
        controller.create(request);
        verify(service).create(request);
    }

    @Test
    @DisplayName("Create should return 200 OK")
    void createShouldReturn200Ok(@Random CreateClientRequest request) {
        var result = controller.create(request);
        assertThat(result).hasFieldOrPropertyWithValue("status", OK);
    }

    @Test
    @DisplayName("Create should return client from service")
    void createShouldReturnClientFromService(@Random CreateClientRequest request, @Random ClientResponse details) {
        details.setName(request.getName());
        when(service.create(argThat(request::equals)))
                .thenReturn(details);
        var result = controller.create(request);
        assertThat(result.getBody())
                .usingRecursiveComparison().isEqualTo(details);
    }
}
