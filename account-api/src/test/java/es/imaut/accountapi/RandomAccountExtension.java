package es.imaut.accountapi;

import io.github.benas.randombeans.api.Randomizer;
import io.github.glytching.junit.extension.random.RandomBeansExtension;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandomBuilder;
import static io.github.benas.randombeans.FieldPredicates.named;
import static io.github.benas.randombeans.randomizers.text.StringRandomizer.aNewStringRandomizer;

public final class RandomAccountExtension extends RandomBeansExtension {
    private static final String EMAIL_FORMAT = "%s@%s.%s";
    private static final Randomizer<String> emailRandomizer = () -> EMAIL_FORMAT.formatted(
            aNewStringRandomizer(5, 10, 1L).getRandomValue(),
            aNewStringRandomizer(5, 10, 1L).getRandomValue(),
            aNewStringRandomizer(2, 3, 1L).getRandomValue());

    public RandomAccountExtension() {
        super(aNewEnhancedRandomBuilder()
                .randomize(named("phone"), aNewStringRandomizer(15))
                .randomize(named("type"), aNewStringRandomizer(31))
                .randomize(named("email"), emailRandomizer)
                .randomize(named("iban"), aNewStringRandomizer(24))
                .randomize(named("bic"), aNewStringRandomizer(8))
                .build());
    }
}
