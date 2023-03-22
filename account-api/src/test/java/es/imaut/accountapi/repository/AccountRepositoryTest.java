package es.imaut.accountapi.repository;

import es.imaut.accountapi.RandomAccountExtension;
import es.imaut.accountapi.model.Account;
import io.github.glytching.junit.extension.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ExtendWith({ RandomAccountExtension.class })
@ActiveProfiles("test")
class AccountRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private AccountRepository repository;

    @Test
    @DisplayName("Find all should return empty list")
    void findAllShouldReturnEmptyList() {
        var result = repository.findAll();
        assertThat(result).isNotNull()
                .asList().isEmpty();
    }

    @Test
    @DisplayName("Find all should return all accounts")
    void findAllShouldReturnAllAccounts(@Random(type = Account.class) List<Account> accounts) {
        accounts.stream().peek(a -> {
            a.setId(null);
            a.getBankDetails().forEach(bd -> {
                bd.setId(null);
                bd.setAccount(a);
            });
        }).forEach(entityManager::persist);
        var result = repository.findAll();
        assertThat(result).isNotNull()
                .asList().hasSameSizeAs(accounts)
                .containsExactlyInAnyOrderElementsOf(accounts);
    }
}
