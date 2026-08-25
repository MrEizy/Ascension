package net.zic.ascension.impl.core.damage;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageProfile;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.stats.Stat;

public final class AscensionDamageProfileResolver {
    private AscensionDamageProfileResolver() {
    }


    public static double rawDamage(LivingEntity attacker, AscensionDamageProfile profile) {
        if (attacker == null || profile == null) {
            return 0.0D;
        }
        double damage = profile.baseDamage();
        damage += weaponDamage(attacker) * profile.weaponMultiplier();
        for (var entry : profile.statScaling().entrySet()) {
            damage += stat(attacker, entry.getKey()) * entry.getValue();
        }
        for (var entry : profile.attributeScaling().entrySet()) {
            damage += attribute(attacker, entry.getKey()) * entry.getValue();
        }
        return Double.isFinite(damage) ? Math.max(0.0D, profile.clamp(damage)) : 0.0D;
    }

    public static double weaponDamage(LivingEntity attacker) {
        ItemStack stack = attacker.getMainHandItem();
        if (stack.isEmpty()) {
            return 0.0D;
        }

        double[] add = {0.0D};
        double[] multiplyBase = {0.0D};
        double[] multiplyTotal = {1.0D};
        stack.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (!attribute.equals(Attributes.ATTACK_DAMAGE)) {
                return;
            }
            switch (modifier.operation()) {
                case ADD_VALUE -> add[0] += modifier.amount();
                case ADD_MULTIPLIED_BASE -> multiplyBase[0] += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multiplyTotal[0] *= 1.0D + modifier.amount();
            }
        });
        return Math.max(0.0D, add[0] * (1.0D + multiplyBase[0]) * multiplyTotal[0]);
    }

    public static double stat(LivingEntity attacker, Identifier statId) {
        Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(statId);
        if (stat == null) {
            return 0.0D;
        }
        var provider = attacker.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        return provider == null || provider.getData() == null
                ? 0.0D
                : provider.getData().getSource().getStat(stat);
    }

    public static double attribute(LivingEntity attacker, Identifier attributeId) {
        Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.get(attributeId).orElse(null);
        if (attribute == null) {
            return 0.0D;
        }
        if (attacker.getAttributes().hasAttribute(attribute)) {
            return attacker.getAttributeValue(attribute);
        }
        ZenithAttributeHolder holder = attacker.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        return holder.hasAttribute(attribute) ? holder.getAttribute(attribute).getValue() : 0.0D;
    }
}
