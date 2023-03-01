package es.imaut.clientapi.service;

import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.exception.ClientNotFoundException;
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

import javax.json.JsonMergePatch;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.json.Json.createMergePatch;
import static javax.json.Json.createObjectBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({ MockitoExtension.class, RandomBeansExtension.class })
class ClientServiceTest {
    @Mock
    private ClientRepository repository;
    @Mock
    private ClientMapper clientMapper = new ClientMapperImpl();
    @Spy
    private ClientDetailsMapper detailsMapper = new ClientDetailsMapperImpl();
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
    @DisplayName("Find by id should call repository")
    void findByIdShouldCallRepository(@Random Client client) {
        when(repository.findById(client.getId())).thenReturn(of(client));
        service.findById(client.getId());
        verify(repository).findById(client.getId());
    }

    @Test
    @DisplayName("Find by id should throw not found exception")
    void findByIdShouldThrowNotFoundException() {
        when(repository.findById(anyLong())).thenReturn(empty());
        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    @DisplayName("Find by id should return client from repository")
    void findAllShouldReturnClientFromRepository(@Random Client client) {
        when(repository.findById(client.getId())).thenReturn(of(client));
        var result = service.findById(client.getId());
        assertThat(result).usingRecursiveComparison().isEqualTo(client);
    }

    @Test
    @DisplayName("Create should call repository")
    void createShouldCallRepository(@Random CreateClientRequest request, @Random Client client) {
        client.setName(request.getName());
        when(clientMapper.from(request))
                .thenReturn(client);
        service.create(request);
        verify(repository).save(argThat(c -> c.getName().equals(request.getName())));
    }

    @Test
    @DisplayName("Create should return created client")
    void createShouldReturnCreatedClient(@Random CreateClientRequest request, @Random Client client) {
        client.setName(request.getName());
        when(clientMapper.from(request))
                .thenReturn(client);
        when(repository.save(argThat(c -> c.getName().equals(request.getName()))))
                .thenReturn(client);
        var result = service.create(request);
        assertThat(result).usingRecursiveComparison().isEqualTo(client);
    }

    @Test
    @DisplayName("Update should call repository to find client by id")
    void updateShouldCallRepositoryToFindClientById(@Random Client client) {
        when(repository.findById(client.getId())).thenReturn(of(client));
        when(repository.save(client)).thenReturn(client);
        when(clientMapper.merge(any(JsonMergePatch.class), any(Client.class)))
                .thenReturn(client);
        service.update(client.getId(), createMergePatch(createObjectBuilder().build()));
        verify(repository).findById(client.getId());
    }

    @Test
    @DisplayName("Update should throw client not found exception")
    void updateShouldThrowClientNotFoundException() {
        when(repository.findById(1L)).thenReturn(empty());
        assertThatThrownBy(() -> service.update(1L, createMergePatch(createObjectBuilder().build())))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    @DisplayName("Update should call repository to save client")
    void updateShouldCallRepositoryToSaveClient(@Random Client client) {
        when(repository.findById(client.getId())).thenReturn(of(client));
        when(repository.save(client)).thenReturn(client);
        when(clientMapper.merge(any(JsonMergePatch.class), any(Client.class)))
                .thenReturn(client);
        service.update(client.getId(), createMergePatch(createObjectBuilder().build()));
        verify(repository).save(client);
    }

    @Test
    @DisplayName("Update should return updated client")
    void updateShouldReturnUpdatedClient(@Random Client client, @Random Client updated) {
        updated.setId(client.getId());
        when(repository.findById(client.getId())).thenReturn(of(client));
        when(repository.save(updated)).thenReturn(updated);
        when(clientMapper.merge(any(JsonMergePatch.class), eq(client)))
                .thenReturn(updated);
        var result = service.update(client.getId(), createMergePatch(createObjectBuilder().build()));
        assertThat(result).usingRecursiveComparison().isEqualTo(updated);
    }

    @Test
    @DisplayName("Delete should call repository to check if exists")
    void deleteShouldNotCallRepositoryToCheckIfExists() {
        service.delete(1L);
        verify(repository).existsById(1L);
    }

    @Test
    @DisplayName("Delete should call repository")
    void deleteShouldNotCallRepositoryDelete() {
        when(repository.existsById(1L)).thenReturn(false);
        service.delete(1L);
        verify(repository, times(0)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete should call repository")
    void deleteShouldCallRepositoryDelete() {
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
