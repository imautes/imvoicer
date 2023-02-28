package es.imaut.clientapi.mapper;

import es.imaut.clientapi.dto.ClientDto;
import es.imaut.clientapi.model.Client;
import org.mapstruct.Mapper;

@Mapper
public interface ClientMapper {
    Client from(ClientDto client);
}
