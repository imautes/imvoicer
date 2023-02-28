package es.imaut.clientapi.repository;

import es.imaut.clientapi.model.Client;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ExtendWith({ RandomBeansExtension.class })
class ClientRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ClientRepository repository;

    @Test
    @DisplayName("Find all should return empty list")
    void findAllShouldReturnEmptyList() {
        var result = repository.findAll();
        assertThat(result).isNotNull()
                .asList().isEmpty();
    }

    @Test
    @DisplayName("Find all should return all clients")
    void findAllShouldReturnAllClients(@Random(type = Client.class) List<Client> clients) {
        clients.forEach(entityManager::persist);
        var result = repository.findAll();
        assertThat(result).isNotNull()
                .asList().hasSameSizeAs(clients)
                .containsExactlyInAnyOrderElementsOf(clients);
    }
}
