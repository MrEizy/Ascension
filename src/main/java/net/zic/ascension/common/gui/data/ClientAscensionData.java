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
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.skill_casting.SkillCastHandler;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.SuppressedZenithAttribute;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;

import java.util.Optional;

public final class ClientAscensionData {
    private ClientAscensionData() {
    }

    public static Optional<Player> getPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }

    public static Optional<AscensionEntityData> getEntityData() {
        return getPlayer().flatMap(player -> {
            AscensionEntityDataProvider holder = player.getCapability(
                    CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
            );
            if (holder == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(holder.getData(player));
        });
    }

    public static Optional<OriginSource> getSource() {
        return getEntityData().map(AscensionEntityData::getSource);
    }

    public static Optional<SkillCastHandler> getSkillCastHandler() {
        return getPlayer().map(player -> player.getData(
                AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER
        ));
    }
    public static double getQi(){

        if(getPlayer().isEmpty()) return 0;

        Player player = getPlayer().get();

        EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if(provider == null) return 0;
        return provider.getQi();

    }
    public static double getMaxQi(){

        if(getPlayer().isEmpty()) return 0;

        Player player = getPlayer().get();

        EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if(provider == null) return 0;
        return provider.getMaxQi();
    }


    public static double getAttributeValue(Holder<Attribute> attributeHolder){
        ZenithAttributeHolder holder = Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        return holder.hasAttribute(attributeHolder) ? holder.getAttribute(attributeHolder).getValue() : 0;

    }
    public static double getUnsuppressedAttributeValue(Holder<Attribute> attributeHolder){
        ZenithAttributeHolder holder = Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        return holder.hasAttribute(attributeHolder) ?
                (holder.isSuppressable(attributeHolder)?((SuppressedZenithAttribute) holder.getAttribute(attributeHolder)).getUnsuppressedValue():0)
        : 0;
    }
    public static double getSuppression(Holder<Attribute> attributeHolder){
        ZenithAttributeHolder holder = Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        return holder.hasAttribute(attributeHolder) ?
                (holder.isSuppressable(attributeHolder)?((SuppressedZenithAttribute) holder.getAttribute(attributeHolder)).getSuppression():1)
                : 1;
    }

    public static double getStamina() {
        return getPlayer().map(StaminaService::getStamina).orElse(0.0D);
    }

    public static double getMaximumStamina() {
        return getPlayer().map(StaminaService::getMaximumStamina).orElse(0.0D);
    }


}
