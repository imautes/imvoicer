package es.imaut.clientapi.mapper;

import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.model.Client;
import org.mapstruct.Mapper;

@Mapper
public interface ClientMapper {
    Client from(CreateClientRequest request);
}
