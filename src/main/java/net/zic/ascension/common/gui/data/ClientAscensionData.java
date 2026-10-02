package net.zic.ascension.common.gui.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.skill_casting.SkillCastHandler;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.SuppressedZenithAttribute;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.stats.ZenithStatHolder;

import java.util.Optional;

public final class ClientAscensionData {
    private ClientAscensionData() {
    }

    public static Optional<Player> getPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }

    public static Optional<AscensionEntityData> getEntityData() {
        return getPlayer().flatMap(player -> {
            AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);

            if (holder == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(holder.getData());
        });
    }

    public static Optional<OriginSource> getSource() {
        return getEntityData().map(AscensionEntityData::getSource);
    }

    public static Optional<SkillCastHandler> getSkillCastHandler() {
        return getPlayer().map(player -> player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER));
    }

    public static double getQi() {
        return getPlayer().map(player -> {
            EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
            return provider == null ? 0.0D : provider.getQi();
        }).orElse(0.0D);
    }

    public static double getMaxQi() {
        return Math.max(0.0D, getAttributeValue(AscensionAttributes.MAX_QI));
    }

    public static double getAttributeValue(Holder<Attribute> attributeHolder) {
        return getPlayer().map(player -> {
            if (player.getAttributes().hasAttribute(attributeHolder)) {
                return player.getAttributeValue(attributeHolder);
            }

            ZenithAttributeHolder holder = player.getData(ZenithAttachments.ATTRIBUTE_HOLDER);

            if (holder.hasAttribute(attributeHolder)) {
                return holder.getAttribute(attributeHolder).getValue();
            }

            return 0.0D;
        }).orElse(0.0D);
    }

    public static double getUnsuppressedAttributeValue(Holder<Attribute> attributeHolder) {
        return getPlayer().map(player -> {
            ZenithAttributeHolder holder = player.getData(ZenithAttachments.ATTRIBUTE_HOLDER);

            if (!holder.hasAttribute(attributeHolder) || !holder.isSuppressable(attributeHolder)) {
                return 0.0D;
            }

            return ((SuppressedZenithAttribute) holder.getAttribute(attributeHolder)).getUnsuppressedValue();
        }).orElse(0.0D);
    }

    public static double getSuppression(Holder<Attribute> attributeHolder) {
        return getPlayer().map(player -> {
            ZenithAttributeHolder holder = player.getData(ZenithAttachments.ATTRIBUTE_HOLDER);

            if (!holder.hasAttribute(attributeHolder)
                    || !holder.isSuppressable(attributeHolder)) {
                return 1.0D;
            }

            return ((SuppressedZenithAttribute) holder.getAttribute(attributeHolder)).getSuppression();
        }).orElse(1.0D);
    }

    public static double getStamina() {
        return getPlayer().map(StaminaService::getStamina).orElse(0.0D);
    }

    public static double getMaximumStamina() {
        return Math.max(0.0D, getAttributeValue(AscensionAttributes.MAX_STAMINA));
    }
}