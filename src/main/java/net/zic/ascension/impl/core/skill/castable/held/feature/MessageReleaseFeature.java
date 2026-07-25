package net.zic.ascension.impl.core.skill.castable.held.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeatureType;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastReleaseFeatureTypes;

public record MessageReleaseFeature(Component message, boolean overlay) implements HeldCastReleaseFeature {
    public static final MapCodec<MessageReleaseFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("message").forGetter(MessageReleaseFeature::message),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("overlay", true)
                    .forGetter(MessageReleaseFeature::overlay)
    ).apply(instance, MessageReleaseFeature::new));

    @Override
    public HeldCastReleaseFeatureType getType() {
        return AscensionHeldCastReleaseFeatureTypes.MESSAGE.get();
    }

    @Override
    public void apply(HeldCastReleaseContext context) {
        if (overlay) {
            context.execution().caster().sendOverlayMessage(message);
        } else {
            context.execution().caster().sendSystemMessage(message);
        }
    }
}
