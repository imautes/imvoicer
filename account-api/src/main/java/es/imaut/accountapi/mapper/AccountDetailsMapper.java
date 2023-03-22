package es.imaut.accountapi.mapper;

import es.imaut.accountapi.domain.AccountResponse;
import es.imaut.accountapi.model.Account;
import org.mapstruct.Mapper;

@Mapper
public interface AccountDetailsMapper {
    AccountResponse from(Account account);
}
