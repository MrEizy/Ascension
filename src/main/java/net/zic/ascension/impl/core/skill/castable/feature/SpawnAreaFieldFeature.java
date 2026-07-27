package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.common.field.AreaFieldService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record SpawnAreaFieldFeature(
        Identifier field
) implements SkillExecutionFeature {
    public static final MapCodec<SpawnAreaFieldFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("field").forGetter(SpawnAreaFieldFeature::field)
    ).apply(instance, SpawnAreaFieldFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.SPAWN_AREA_FIELD.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        AreaFieldService.spawn(context, field, context.position());
    }
}
