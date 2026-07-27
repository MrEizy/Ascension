package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.construct.OwnerBoundConstructView;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.construct.OwnerBoundConstructService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record RestoreConstructFeature(
        Identifier construct,
        ScaledValue amount
) implements SkillExecutionFeature {
    public static final MapCodec<RestoreConstructFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("construct").forGetter(RestoreConstructFeature::construct),
            ScaledValue.CODEC.codec().fieldOf("amount").forGetter(RestoreConstructFeature::amount)
    ).apply(instance, RestoreConstructFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.RESTORE_CONSTRUCT.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        double resolved = amount.resolve(context.scaledValueContext());
        if (!Double.isFinite(resolved) || resolved == 0.0D) {
            return;
        }
        for (OwnerBoundConstructView value : OwnerBoundConstructService.findOwned(
                context.caster().getUUID(),
                construct
        )) {
            OwnerBoundConstructService.modifyStability(value.runtimeId(), resolved);
        }
    }
}
