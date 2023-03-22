package es.imaut.accountapi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.accountapi.domain.CreateAccountRequest;
import es.imaut.accountapi.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import javax.json.JsonMergePatch;
import javax.json.JsonValue;

@Mapper
public abstract class AccountMapper {
    @Autowired
    private ObjectMapper objectMapper;

    public abstract Account from(CreateAccountRequest request);

    public Account merge(JsonMergePatch request, @MappingTarget Account account) {
        var accountJson = objectMapper.convertValue(account, JsonValue.class);
        var patchedJson = request.apply(accountJson);
        return objectMapper.convertValue(patchedJson, Account.class);
    }
}
