package es.imaut.clientapi.service;

import es.imaut.clientapi.domain.ClientResponse;
import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.exception.ClientNotFoundException;
import es.imaut.clientapi.mapper.ClientDetailsMapper;
import es.imaut.clientapi.mapper.ClientMapper;
import es.imaut.clientapi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.json.JsonMergePatch;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository repository;
    private final ClientMapper clientMapper;
    private final ClientDetailsMapper detailsMapper;

    public List<ClientResponse> findAll() {
        return repository.findAll().stream().map(detailsMapper::from).toList();
    }

    public ClientResponse findById(Long id) {
        return repository.findById(id)
                .map(detailsMapper::from)
                .orElseThrow(ClientNotFoundException::new);
    }

    public ClientResponse create(CreateClientRequest request) {
        return detailsMapper.from(repository.save(clientMapper.from(request)));
    }

    public ClientResponse update(Long id, JsonMergePatch patch) {
        return repository.findById(id)
                .map(c -> clientMapper.merge(patch, c))
                .map(repository::save)
                .map(detailsMapper::from)
                .orElseThrow(ClientNotFoundException::new);
    }

    public void delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
    }
}
