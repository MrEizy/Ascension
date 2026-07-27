package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.common.field.AreaFieldService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record RemoveAreaFieldFeature(
        Identifier field
) implements SkillExecutionFeature {
    public static final MapCodec<RemoveAreaFieldFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("field").forGetter(RemoveAreaFieldFeature::field)
    ).apply(instance, RemoveAreaFieldFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.REMOVE_AREA_FIELD.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        AreaFieldService.removeOwned(context.level(), context.caster().getUUID(), field);
    }
}
