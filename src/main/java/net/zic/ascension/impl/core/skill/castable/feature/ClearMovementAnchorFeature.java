package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.common.movement.MovementService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record ClearMovementAnchorFeature(
        Identifier anchor
) implements SkillExecutionFeature {
    public static final MapCodec<ClearMovementAnchorFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("anchor").forGetter(ClearMovementAnchorFeature::anchor)
    ).apply(instance, ClearMovementAnchorFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.CLEAR_MOVEMENT_ANCHOR.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        MovementService.removeAnchor(context.caster(), anchor);
    }
}
