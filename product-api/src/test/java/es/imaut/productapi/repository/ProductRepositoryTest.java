package es.imaut.productapi.repository;

import es.imaut.productapi.RandomProductExtension;
import es.imaut.productapi.model.Product;
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
@ExtendWith({ RandomProductExtension.class })
@ActiveProfiles("test")
class ProductRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ProductRepository repository;

    @Test
    @DisplayName("Find all should return empty list")
    void findAllShouldReturnEmptyList() {
        var result = repository.findAll();
        assertThat(result).isNotNull()
                .asList().isEmpty();
    }

    @Test
    @DisplayName("Find all should return all products")
    void findAllShouldReturnAllProducts(@Random(type = Product.class) List<Product> products) {
        products.stream().peek(c -> c.setId(null)).forEach(entityManager::persist);
        var result = repository.findAll();
        assertThat(result).isNotNull()
                .asList().hasSameSizeAs(products)
                .containsExactlyInAnyOrderElementsOf(products);
    }
}
