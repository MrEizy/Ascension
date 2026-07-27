package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.common.construct.OwnerBoundConstructService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record SpawnConstructFeature(
        Identifier construct
) implements SkillExecutionFeature {
    public static final MapCodec<SpawnConstructFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("construct").forGetter(SpawnConstructFeature::construct)
    ).apply(instance, SpawnConstructFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.SPAWN_CONSTRUCT.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        OwnerBoundConstructService.spawn(context, construct);
    }
}
