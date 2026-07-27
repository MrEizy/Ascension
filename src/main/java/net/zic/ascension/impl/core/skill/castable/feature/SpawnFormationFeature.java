package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.common.formation.FormationService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record SpawnFormationFeature(
        Identifier formation
) implements SkillExecutionFeature {
    public static final MapCodec<SpawnFormationFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("formation").forGetter(SpawnFormationFeature::formation)
    ).apply(instance, SpawnFormationFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.SPAWN_FORMATION.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        FormationService.spawn(context, formation, context.position());
    }
}
