package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.common.construct.OwnerBoundConstructService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record RemoveConstructFeature(
        Identifier construct
) implements SkillExecutionFeature {
    public static final MapCodec<RemoveConstructFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("construct").forGetter(RemoveConstructFeature::construct)
    ).apply(instance, RemoveConstructFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.REMOVE_CONSTRUCT.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        OwnerBoundConstructService.removeOwned(context.level(), context.caster().getUUID(), construct);
    }
}
