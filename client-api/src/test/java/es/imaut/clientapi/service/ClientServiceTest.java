package es.imaut.clientapi.service;

import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.mapper.ClientDetailsMapper;
import es.imaut.clientapi.mapper.ClientDetailsMapperImpl;
import es.imaut.clientapi.mapper.ClientMapper;
import es.imaut.clientapi.mapper.ClientMapperImpl;
import es.imaut.clientapi.model.Client;
import es.imaut.clientapi.repository.ClientRepository;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, RandomBeansExtension.class })
class ClientServiceTest {
    @Mock
    private ClientRepository repository;
    @Spy
    private ClientMapper mapper = new ClientMapperImpl();
    @Spy
    private ClientDetailsMapper dtoMapper = new ClientDetailsMapperImpl();
    @InjectMocks
    private ClientService service;

    @Test
    @DisplayName("Find all should call repository")
    void findAllShouldCallRepository() {
        service.findAll();
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Find all should return empty list")
    void findAllShouldReturnEmptyList() {
        var result = service.findAll();
        assertThat(result).isNotNull()
                .asList().isEmpty();
    }

    @Test
    @DisplayName("Find all should return list mapped from repository")
    void findAllShouldReturnListMappedFromRepository(@Random(type = Client.class) List<Client> clients) {
        when(repository.findAll()).thenReturn(clients);
        var result = service.findAll();
        assertThat(result).isNotNull()
                .asList().hasSameSizeAs(clients)
                .usingRecursiveFieldByFieldElementComparator().isEqualTo(clients);
    }

    @Test
    @DisplayName("Create should call repository")
    void createShouldCallRepository(@Random CreateClientRequest request) {
        service.create(request);
        verify(repository).save(argThat(c -> c.getName().equals(request.getName())));
    }

    @Test
    @DisplayName("Create should return created client")
    void createShouldReturnCreatedClient(@Random CreateClientRequest request, @Random Client client) {
        client.setName(request.getName());
        when(repository.save(argThat(c -> c.getName().equals(request.getName()))))
                .thenReturn(client);
        var result = service.create(request);
        assertThat(result).usingRecursiveComparison().isEqualTo(client);
    }
}
