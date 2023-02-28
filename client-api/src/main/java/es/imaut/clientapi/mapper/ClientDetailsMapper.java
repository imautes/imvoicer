package es.imaut.clientapi.mapper;

import es.imaut.clientapi.domain.ClientResponse;
import es.imaut.clientapi.model.Client;
import org.mapstruct.Mapper;

@Mapper
public interface ClientDetailsMapper {
    ClientResponse from(Client client);
}
