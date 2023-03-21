package es.imaut.productapi;

import io.github.glytching.junit.extension.random.RandomBeansExtension;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandomBuilder;
import static io.github.benas.randombeans.FieldPredicates.named;
import static io.github.benas.randombeans.randomizers.number.BigDecimalRandomizer.aNewBigDecimalRandomizer;
import static io.github.benas.randombeans.randomizers.text.StringRandomizer.aNewStringRandomizer;
import static java.lang.Integer.valueOf;

public final class RandomProductExtension extends RandomBeansExtension {
    public RandomProductExtension() {
        super(aNewEnhancedRandomBuilder()
                .randomize(named("currency"), aNewStringRandomizer(3))
                .randomize(named("unit"), aNewStringRandomizer(15))
                .randomize(named("netPrice"), aNewBigDecimalRandomizer(valueOf(4)))
                .build());
    }
}
