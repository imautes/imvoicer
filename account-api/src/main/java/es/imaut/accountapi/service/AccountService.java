package es.imaut.accountapi.service;

import es.imaut.accountapi.domain.AccountResponse;
import es.imaut.accountapi.domain.CreateAccountRequest;
import es.imaut.accountapi.exception.AccountNotFoundException;
import es.imaut.accountapi.mapper.AccountDetailsMapper;
import es.imaut.accountapi.mapper.AccountMapper;
import es.imaut.accountapi.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.json.JsonMergePatch;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository repository;
    private final AccountMapper accountMapper;
    private final AccountDetailsMapper detailsMapper;

    public List<AccountResponse> findAll() {
        return repository.findAll().stream().map(detailsMapper::from).toList();
    }

    public AccountResponse findById(Long id) {
        return repository.findById(id)
                .map(detailsMapper::from)
                .orElseThrow(AccountNotFoundException::new);
    }

    public AccountResponse create(CreateAccountRequest request) {
        return detailsMapper.from(repository.save(accountMapper.from(request)));
    }

    public AccountResponse update(Long id, JsonMergePatch patch) {
        return repository.findById(id)
                .map(c -> accountMapper.merge(patch, c))
                .map(repository::save)
                .map(detailsMapper::from)
                .orElseThrow(AccountNotFoundException::new);
    }

    public void delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
    }
}
