package es.imaut.clientapi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import javax.json.JsonMergePatch;
import javax.json.JsonValue;

@Mapper
public abstract class ClientMapper {
    @Autowired
    private ObjectMapper objectMapper;

    public abstract Client from(CreateClientRequest request);

    public Client merge(JsonMergePatch request, @MappingTarget Client client) {
        var clientJson = objectMapper.convertValue(client, JsonValue.class);
        var patchedJson = request.apply(clientJson);
        return objectMapper.convertValue(patchedJson, Client.class);
    }
}
