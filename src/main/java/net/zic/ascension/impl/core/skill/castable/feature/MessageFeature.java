package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record MessageFeature(Component message, boolean overlay) implements SkillExecutionFeature {
    public static final MapCodec<MessageFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("message").forGetter(MessageFeature::message),
            Codec.BOOL.optionalFieldOf("overlay", true).forGetter(MessageFeature::overlay)
    ).apply(instance, MessageFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.MESSAGE.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        if (overlay) {
            context.caster().sendOverlayMessage(message);
        } else {
            context.caster().sendSystemMessage(message);
        }
    }
}
