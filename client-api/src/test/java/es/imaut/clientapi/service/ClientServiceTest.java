package es.imaut.clientapi.service;

import es.imaut.clientapi.mapper.ClientDtoMapper;
import es.imaut.clientapi.mapper.ClientDtoMapperImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, RandomBeansExtension.class})
class ClientServiceTest {
    @Mock
    private ClientRepository repository;
    @Spy
    private ClientDtoMapper dtoMapper = new ClientDtoMapperImpl();
    @InjectMocks
    private ClientService service;

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
}
