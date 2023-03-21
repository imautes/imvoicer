package es.imaut.productapi.mapper;

import es.imaut.productapi.config.ObjectMapperConfig;
import es.imaut.productapi.mapper.ProductMapper;
import es.imaut.productapi.model.Product;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static javax.json.Json.createMergePatch;
import static javax.json.Json.createObjectBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = { ProductMapperImpl.class })
@Import({ ObjectMapperConfig.class })
@ExtendWith({ RandomBeansExtension.class })
class ProductMapperTest {
    @Autowired
    private ProductMapper mapper;

    @Test
    @DisplayName("Merge should apply new value")
    void mergeShouldApplyNewValue() {
        var patch = createMergePatch(createObjectBuilder()
                .add("name", "Name")
                .build());
        assertThat(mapper.merge(patch, new Product()))
                .hasFieldOrPropertyWithValue("name", "Name");
    }

    @Test
    @DisplayName("Merge should apply update value")
    void mergeShouldApplyUpdateValue(@Random Product product) {
        var id = product.getId();
        var patch = createMergePatch(createObjectBuilder()
                .add("name", "Name")
                .build());
        assertThat(mapper.merge(patch, product))
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", "Name");
    }

    @Test
    @DisplayName("Merge should not apply anything")
    void mergeShouldNotApplyAnything(@Random Product product) {
        var id = product.getId();
        var name = product.getName();
        var patch = createMergePatch(createObjectBuilder().build());
        assertThat(mapper.merge(patch, product))
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", name);
    }

    @Test
    @DisplayName("Merge should apply null value")
    void mergeShouldApplyNullValue(@Random Product product) {
        var id = product.getId();
        var patch = createMergePatch(createObjectBuilder()
                .addNull("name")
                .build());
        assertThat(mapper.merge(patch, product))
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", null);
    }

    @Test
    @DisplayName("Merge should ignore unknown fields")
    void mergeShouldIgnoreUnknownFields(@Random Product product) {
        var id = product.getId();
        var name = product.getName();
        var patch = createMergePatch(createObjectBuilder()
                .add("ignored", "field")
                .build());
        assertThat(mapper.merge(patch, product))
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", name);
    }
}
