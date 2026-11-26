package net.zic.ascension.impl.core.attribute;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.attribute.AttributeScalingMath;
import net.zic.ascension.configuration.ConfigurationRegistries;
import net.zic.ascension.configuration.cultivation.AttributeScalingDefinition;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.custom_attributes.ZenithAttribute;
import net.zic.zenithlib.stats.EntityStatHolder;
import net.zic.zenithlib.value_containers.typed.Modifier;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public final class ScaledAttributes {
    private static final Identifier DEFAULT = AscensionCraft.prefix("default");
    private static final Identifier MODIFIER = AscensionCraft.prefix("dynamic_attribute_scaling");
    private static final TagKey<EntityType<?>> MOBS = TagKey.create(Registries.ENTITY_TYPE, AscensionCraft.prefix("stat_scaling/mobs"));
    private static final String PROCESS = "ascension_dynamic_attribute_scaling";
    private ScaledAttributes() {}

    public static boolean applies(LivingEntity entity) {
        return entity instanceof Player || entity.getType().builtInRegistryHolder().is(MOBS);
    }

    public static AttributeScalingDefinition profile(LivingEntity entity) {
        return entity.registryAccess().lookup(ConfigurationRegistries.ATTRIBUTE_SCALING_REGISTRY.key())
                .map(registry -> registry.getValue(DEFAULT)).orElse(null);
    }

    public static void refresh(LivingEntity entity) {
        if (entity.level().isClientSide() || !applies(entity)) return;
        var profile = profile(entity);
        if (profile == null) return;
        var stats = entity.getData(ZenithAttachments.STAT_HOLDER);
        var attributes = entity.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        List<Pending> updates = new ArrayList<>();
        collect(entity, attributes, stats, profile.common(), updates);
        if (entity instanceof Player) collect(entity, attributes, stats, profile.player(), updates);
        if (updates.isEmpty()) return;
        attributes.startProcess(PROCESS);
        try {
            for (Pending update : updates) {
                var attribute = update.attribute();
                attribute.startProcess(PROCESS);
                try {
                    attribute.removeModifier(MODIFIER);
                    if (update.contribution() > 0) attribute.addFlatModifier(Modifier.flat(MODIFIER, 0, update.contribution()));
                } finally {
                    attribute.resolveProcess(PROCESS);
                }
            }
        } finally {
            attributes.resolveProcess(PROCESS);
        }
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
        entity.syncData(ZenithAttachments.ATTRIBUTE_HOLDER);
    }

    private record Pending(ZenithAttribute attribute, double contribution) {}

    private static void collect(LivingEntity entity, ZenithAttributeHolder holder, EntityStatHolder stats,
                                Map<Identifier, List<AttributeScalingDefinition.Curve>> definitions, List<Pending> updates) {
        for (var entry : definitions.entrySet()) {
            var key = BuiltInRegistries.ATTRIBUTE.get(entry.getKey()).orElse(null);
            if (key == null || !entity.getAttributes().hasAttribute(key) || !holder.hasAttribute(key)) continue;
            var attribute = holder.getAttribute(key);
            if (attribute == null) continue;
            double contribution = 0;
            for (var curve : entry.getValue()) {
                var stat = ZenithRegistries.STAT_REGISTRY.getValue(curve.stat());
                if (stat == null || stats.getStatInstance(stat) == null) continue;
                contribution += curve.at(stats.getStat(stat));
            }
            contribution = AttributeScalingMath.finiteContribution(contribution);
            var existing = attribute.getFlatModifier(MODIFIER);
            if (existing == null ? contribution == 0 : Double.compare(existing.value(), contribution) == 0) continue;
            updates.add(new Pending(attribute, contribution));
        }
    }
}
