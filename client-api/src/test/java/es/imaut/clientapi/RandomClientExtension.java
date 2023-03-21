package es.imaut.clientapi;

import io.github.glytching.junit.extension.random.RandomBeansExtension;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandomBuilder;
import static io.github.benas.randombeans.FieldPredicates.named;
import static io.github.benas.randombeans.randomizers.text.StringRandomizer.aNewStringRandomizer;

public final class RandomClientExtension extends RandomBeansExtension {
    public RandomClientExtension() {
        super(aNewEnhancedRandomBuilder()
                .randomize(named("vatNumber"), aNewStringRandomizer(31))
                .randomize(named("postcode"), aNewStringRandomizer(15))
                .build());
    }
}
