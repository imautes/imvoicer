package es.imaut.clientapi.service;

import es.imaut.clientapi.dto.ClientDto;
import es.imaut.clientapi.mapper.ClientDtoMapper;
import es.imaut.clientapi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository repository;
    private final ClientDtoMapper dtoMapper;

    public List<ClientDto> findAll() {
        return repository.findAll().stream().map(dtoMapper::from).toList();
    }
}
