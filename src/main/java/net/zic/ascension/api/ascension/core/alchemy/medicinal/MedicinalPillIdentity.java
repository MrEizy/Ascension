package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import java.util.List;

/**
 * Defines a family of names for a resolved pill effect using a datapack entry.
 * The stored names are translation keys selected deterministically from a pill batch.
 */
public record MedicinalPillIdentity(Identifier effect, List<String> names) {
    public static final Codec<MedicinalPillIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("effect").forGetter(MedicinalPillIdentity::effect),
            Codec.STRING.listOf().fieldOf("names").forGetter(MedicinalPillIdentity::names)
    ).apply(instance, MedicinalPillIdentity::new));

    public MedicinalPillIdentity {
        if (effect == null) throw new IllegalArgumentException("A pill identity requires an effect");
        names = names == null ? List.of() : names.stream().filter(s -> s != null && !s.isBlank())
                .limit(16).toList();
        if (names.isEmpty()) throw new IllegalArgumentException("A pill identity requires at least one name");
    }
}
