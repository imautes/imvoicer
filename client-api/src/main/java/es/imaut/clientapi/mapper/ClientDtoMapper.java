package es.imaut.clientapi.mapper;

import es.imaut.clientapi.domain.ClientDto;
import es.imaut.clientapi.model.Client;
import org.mapstruct.Mapper;

@Mapper
public interface ClientDtoMapper {
    ClientDto from(Client client);
}
