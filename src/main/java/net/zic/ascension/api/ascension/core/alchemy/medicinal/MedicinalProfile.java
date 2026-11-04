package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.List;

public record MedicinalProfile(MedicinalComposition composition, List<Effect> effects, int schemaVersion) {
    public static final Codec<MedicinalProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MedicinalComposition.CODEC.fieldOf("composition").forGetter(MedicinalProfile::composition),
            Effect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(MedicinalProfile::effects),
            Codec.INT.optionalFieldOf("schema_version", 1).forGetter(MedicinalProfile::schemaVersion)
    ).apply(instance, MedicinalProfile::new));

    public MedicinalProfile(MedicinalComposition composition, List<Effect> effects) {
        this(composition, effects, 1);
    }

    public MedicinalProfile {
        schemaVersion = Mth.clamp(schemaVersion, 1, 16);
        composition = composition == null ? MedicinalComposition.EMPTY : composition;
        effects = effects == null ? List.of() : List.copyOf(effects.stream().limit(32).toList());
    }

    public record Effect(Identifier id, double potency, int durationTicks, boolean adverse) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(Effect::id),
                Codec.DOUBLE.fieldOf("potency").forGetter(Effect::potency),
                Codec.INT.optionalFieldOf("duration_ticks", 0).forGetter(Effect::durationTicks),
                Codec.BOOL.optionalFieldOf("adverse", false).forGetter(Effect::adverse)
        ).apply(instance, Effect::new));

        public Effect {
            potency = Double.isFinite(potency) ? Mth.clamp(potency, 0.0D, 4096.0D) : 0.0D;
            durationTicks = Mth.clamp(durationTicks, 0, 72000);
        }
    }
}
