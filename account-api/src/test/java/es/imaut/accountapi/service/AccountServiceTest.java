package es.imaut.accountapi.service;

import es.imaut.accountapi.domain.CreateAccountRequest;
import es.imaut.accountapi.exception.AccountNotFoundException;
import es.imaut.accountapi.mapper.AccountDetailsMapper;
import es.imaut.accountapi.mapper.AccountDetailsMapperImpl;
import es.imaut.accountapi.mapper.AccountMapper;
import es.imaut.accountapi.mapper.AccountMapperImpl;
import es.imaut.accountapi.model.Account;
import es.imaut.accountapi.repository.AccountRepository;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.json.JsonMergePatch;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.json.Json.createMergePatch;
import static javax.json.Json.createObjectBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({ MockitoExtension.class, RandomBeansExtension.class })
class AccountServiceTest {
    @Mock
    private AccountRepository repository;
    @Mock
    private AccountMapper accountMapper = new AccountMapperImpl();
    @Spy
    private AccountDetailsMapper detailsMapper = new AccountDetailsMapperImpl();
    @InjectMocks
    private AccountService service;

    @Test
    @DisplayName("Find all should call repository")
    void findAllShouldCallRepository() {
        service.findAll();
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Find all should return empty list")
    void findAllShouldReturnEmptyList() {
        var result = service.findAll();
        assertThat(result).isNotNull()
                .asList().isEmpty();
    }

    @Test
    @DisplayName("Find all should return list mapped from repository")
    void findAllShouldReturnListMappedFromRepository(@Random(type = Account.class) List<Account> accounts) {
        when(repository.findAll()).thenReturn(accounts);
        var result = service.findAll();
        assertThat(result).isNotNull()
                .asList().hasSameSizeAs(accounts)
                .usingRecursiveFieldByFieldElementComparator().isEqualTo(accounts);
    }

    @Test
    @DisplayName("Find by id should call repository")
    void findByIdShouldCallRepository(@Random Account account) {
        when(repository.findById(account.getId())).thenReturn(of(account));
        service.findById(account.getId());
        verify(repository).findById(account.getId());
    }

    @Test
    @DisplayName("Find by id should throw not found exception")
    void findByIdShouldThrowNotFoundException() {
        when(repository.findById(anyLong())).thenReturn(empty());
        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Find by id should return account from repository")
    void findAllShouldReturnAccountFromRepository(@Random Account account) {
        when(repository.findById(account.getId())).thenReturn(of(account));
        var result = service.findById(account.getId());
        assertThat(result).usingRecursiveComparison().isEqualTo(account);
    }

    @Test
    @DisplayName("Create should call repository")
    void createShouldCallRepository(@Random CreateAccountRequest request, @Random Account account) {
        account.setName(request.getName());
        when(accountMapper.from(request))
                .thenReturn(account);
        service.create(request);
        verify(repository).save(argThat(c -> c.getName().equals(request.getName())));
    }

    @Test
    @DisplayName("Create should return created account")
    void createShouldReturnCreatedAccount(@Random CreateAccountRequest request, @Random Account account) {
        account.setName(request.getName());
        when(accountMapper.from(request))
                .thenReturn(account);
        when(repository.save(argThat(c -> c.getName().equals(request.getName()))))
                .thenReturn(account);
        var result = service.create(request);
        assertThat(result).usingRecursiveComparison().isEqualTo(account);
    }

    @Test
    @DisplayName("Update should call repository to find account by id")
    void updateShouldCallRepositoryToFindAccountById(@Random Account account) {
        when(repository.findById(account.getId())).thenReturn(of(account));
        when(repository.save(account)).thenReturn(account);
        when(accountMapper.merge(any(JsonMergePatch.class), any(Account.class)))
                .thenReturn(account);
        service.update(account.getId(), createMergePatch(createObjectBuilder().build()));
        verify(repository).findById(account.getId());
    }

    @Test
    @DisplayName("Update should throw account not found exception")
    void updateShouldThrowAccountNotFoundException() {
        when(repository.findById(1L)).thenReturn(empty());
        assertThatThrownBy(() -> service.update(1L, createMergePatch(createObjectBuilder().build())))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Update should call repository to save account")
    void updateShouldCallRepositoryToSaveAccount(@Random Account account) {
        when(repository.findById(account.getId())).thenReturn(of(account));
        when(repository.save(account)).thenReturn(account);
        when(accountMapper.merge(any(JsonMergePatch.class), any(Account.class)))
                .thenReturn(account);
        service.update(account.getId(), createMergePatch(createObjectBuilder().build()));
        verify(repository).save(account);
    }

    @Test
    @DisplayName("Update should return updated account")
    void updateShouldReturnUpdatedAccount(@Random Account account, @Random Account updated) {
        updated.setId(account.getId());
        when(repository.findById(account.getId())).thenReturn(of(account));
        when(repository.save(updated)).thenReturn(updated);
        when(accountMapper.merge(any(JsonMergePatch.class), eq(account)))
                .thenReturn(updated);
        var result = service.update(account.getId(), createMergePatch(createObjectBuilder().build()));
        assertThat(result).usingRecursiveComparison().isEqualTo(updated);
    }

    @Test
    @DisplayName("Delete should call repository to check if exists")
    void deleteShouldNotCallRepositoryToCheckIfExists() {
        service.delete(1L);
        verify(repository).existsById(1L);
    }

    @Test
    @DisplayName("Delete should call repository")
    void deleteShouldNotCallRepositoryDelete() {
        when(repository.existsById(1L)).thenReturn(false);
        service.delete(1L);
        verify(repository, times(0)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete should call repository")
    void deleteShouldCallRepositoryDelete() {
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
