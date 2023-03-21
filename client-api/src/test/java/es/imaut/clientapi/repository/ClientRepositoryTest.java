package es.imaut.clientapi.repository;

import es.imaut.clientapi.RandomClientExtension;
import es.imaut.clientapi.model.Client;
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
@ExtendWith({ RandomClientExtension.class })
@ActiveProfiles("test")
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
        clients.stream().peek(c -> c.setId(null)).forEach(entityManager::persist);
        var result = repository.findAll();
        assertThat(result).isNotNull()
                .asList().hasSameSizeAs(clients)
                .containsExactlyInAnyOrderElementsOf(clients);
    }
}
