package es.imaut.accountapi.mapper;

import es.imaut.accountapi.config.ObjectMapperConfig;
import es.imaut.accountapi.model.Account;
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

@SpringBootTest(classes = { AccountMapperImpl.class })
@Import({ ObjectMapperConfig.class })
@ExtendWith({ RandomBeansExtension.class })
class AccountMapperTest {
    @Autowired
    private AccountMapper mapper;

    @Test
    @DisplayName("Merge should apply new value")
    void mergeShouldApplyNewValue() {
        var patch = createMergePatch(createObjectBuilder()
                .add("name", "Name")
                .build());
        assertThat(mapper.merge(patch, new Account()))
                .hasFieldOrPropertyWithValue("name", "Name");
    }

    @Test
    @DisplayName("Merge should apply update value")
    void mergeShouldApplyUpdateValue(@Random Account account) {
        var id = account.getId();
        var patch = createMergePatch(createObjectBuilder()
                .add("name", "Name")
                .build());
        assertThat(mapper.merge(patch, account))
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", "Name");
    }

    @Test
    @DisplayName("Merge should not apply anything")
    void mergeShouldNotApplyAnything(@Random Account account) {
        var id = account.getId();
        var name = account.getName();
        var patch = createMergePatch(createObjectBuilder().build());
        assertThat(mapper.merge(patch, account))
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", name);
    }

    @Test
    @DisplayName("Merge should apply null value")
    void mergeShouldApplyNullValue(@Random Account account) {
        var id = account.getId();
        var patch = createMergePatch(createObjectBuilder()
                .addNull("name")
                .build());
        assertThat(mapper.merge(patch, account))
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", null);
    }

    @Test
    @DisplayName("Merge should ignore unknown fields")
    void mergeShouldIgnoreUnknownFields(@Random Account account) {
        var id = account.getId();
        var name = account.getName();
        var patch = createMergePatch(createObjectBuilder()
                .add("ignored", "field")
                .build());
        assertThat(mapper.merge(patch, account))
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", name);
    }
}
