package es.imaut.clientapi.service;

import es.imaut.clientapi.domain.ClientDetails;
import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.mapper.ClientDetailsMapper;
import es.imaut.clientapi.mapper.ClientMapper;
import es.imaut.clientapi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository repository;
    private final ClientMapper mapper;
    private final ClientDetailsMapper detailsMapper;

    public List<ClientDetails> findAll() {
        return repository.findAll().stream().map(detailsMapper::from).toList();
    }

    public ClientDetails create(CreateClientRequest request) {
        return detailsMapper.from(repository.save(mapper.from(request)));
    }
}
