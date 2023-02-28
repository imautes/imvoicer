package es.imaut.clientapi.mapper;

import es.imaut.clientapi.domain.ClientDetails;
import es.imaut.clientapi.model.Client;
import org.mapstruct.Mapper;

@Mapper
public interface ClientDetailsMapper {
    ClientDetails from(Client client);
}
