package net.zic.ascension.api.ascension.core.skill.castable.feature.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.skill.castable.feature.ExecutionSubject;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.HexColorCodec;
import net.zic.ascension.api.ascension.value.ScaledValue;

// TODO: point this at wherever your feature type registry actually lives.
import net.zic.ascension.impl.core.skill.castable.feature.SkillExecutionFeatureTypes;

import net.zic.ascension.network.ClientboundDivineSensePacket;
import net.zic.ascension.network.DivineSenseNetworking;

import java.util.ArrayList;
import java.util.List;

/**
 * "ascension:divine_sense" — expands an outward scan from the caster and,
 * for anything caught inside it, tells ONLY the caster's client to render a
 * glow-style highlight. Nothing is applied server-side as a real status
 * effect, so no other player's client ever learns which entities were hit.
 *
 * The tint color is per-cast JSON config so different techniques that grant
 * Divine Sense can each hand this feature a different hex color without
 * needing separate feature types.
 */
public record DivineSenseFeature(
        ScaledValue radius,
        ScaledValue durationTicks,
        int color,
        boolean includeItems,
        boolean includeSelf
) implements SkillExecutionFeature {

    public static final MapCodec<DivineSenseFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.COMPACT_CODEC.optionalFieldOf("radius", ScaledValue.constant(16.0D)).forGetter(DivineSenseFeature::radius),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(100.0D)).forGetter(DivineSenseFeature::durationTicks),
            HexColorCodec.CODEC.optionalFieldOf("color", 0xFFFFFF).forGetter(DivineSenseFeature::color),
            Codec.BOOL.optionalFieldOf("include_items", true).forGetter(DivineSenseFeature::includeItems),
            Codec.BOOL.optionalFieldOf("include_self", false).forGetter(DivineSenseFeature::includeSelf)
    ).apply(instance, DivineSenseFeature::new));

    public DivineSenseFeature {
        color = color & 0xFFFFFF;
    }

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return SkillExecutionFeatureTypes.DIVINE_SENSE.get();
    }

    @Override
    public ExecutionSubject subject() {
        return ExecutionSubject.CASTER;
    }

    @Override
    public void apply(SkillExecutionContext context) {
        if (!(context.caster() instanceof ServerPlayer player)) {
            return; // only players have a client to privately render the highlight on
        }

        double resolvedRadius = Math.max(0.0D, radius.resolve(context.scaledValueContext()));
        if (resolvedRadius <= 0.0D) {
            return;
        }
        int resolvedDuration = Math.max(1, (int) Math.round(durationTicks.resolve(context.scaledValueContext())));

        Vec3 center = player.position();
        AABB area = AABB.ofSize(center, resolvedRadius * 2.0D, resolvedRadius * 2.0D, resolvedRadius * 2.0D);

        List<Integer> highlighted = new ArrayList<>();
        for (Entity entity : player.level().getEntities(includeSelf ? null : player, area, this::isValidTarget)) {
            if (entity.position().distanceToSqr(center) <= resolvedRadius * resolvedRadius) {
                highlighted.add(entity.getId());
            }
        }

        if (highlighted.isEmpty()) {
            return;
        }

        DivineSenseNetworking.sendToPlayer(player, new ClientboundDivineSensePacket(
                center, (float) resolvedRadius, resolvedDuration, color, highlighted
        ));
    }

    private boolean isValidTarget(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return living.isAlive();
        }
        return includeItems && entity instanceof ItemEntity item && item.isAlive();
    }
}