package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.util.CultivationUtil;
import net.zic.ascension.network.alchemy.ClientboundMedicinalCultivationProgressPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.stamina.StaminaService;

/**
 * Define effects that pills can have in this class
  */
public final class ModPills {

    public static final PillItem.Definition FASTING = PillItem.Definition.builder((level, player, stack, data) -> {
                FoodData food = player.getFoodData();
                food.setFoodLevel(20);
                food.setSaturation(20.0F);
            })
            .canConsume((level, player, stack, data) -> {
                FoodData food = player.getFoodData();
                return food.getFoodLevel() < 20 || food.getSaturationLevel() < 20.0F;
            })
            .effectDescription(data -> Component.translatable("ascension.pill.fasting_pill.effect"))
            .build();

    public static final PillItem.Definition QI_REPLENISHING = PillItem.Definition.builder((level, player, stack, data) -> {
                EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                if (provider != null && provider.getMaxQi() > 0.0D) {
                    provider.regenQi(provider.getMaxQi() * restoreFraction(data, 0.30D));
                }
            })
            .canConsume((level, player, stack, data) -> {
                EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                return provider != null && provider.getMaxQi() > 0.0D && provider.getQi() < provider.getMaxQi();
            })
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.qi_replenishing_pill.effect",
                    Math.round(restoreFraction(data, 0.30D) * 100.0D)
            ))
            .build();

    public static final PillItem.Definition REGENERATION = PillItem.Definition.builder((level, player, stack, data) ->
                    player.heal((float) (player.getMaxHealth() * restoreFraction(data, 0.20D))))
            .canConsume((level, player, stack, data) -> player.getHealth() < player.getMaxHealth())
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.regeneration_pill.effect",
                    Math.round(restoreFraction(data, 0.20D) * 100.0D)
            ))
            .build();

    public static final PillItem.Definition STAMINA_REPLENISHING = PillItem.Definition.builder((level, player, stack, data) -> {
                double maximum = StaminaService.getMaximumStamina(player);
                if (maximum > 0.0D) {
                    StaminaService.restore(
                            player,
                            AscensionResourceSources.DIRECT,
                            maximum * restoreFraction(data, 0.40D)
                    );
                }
            })
            .canConsume((level, player, stack, data) -> {
                double maximum = StaminaService.getMaximumStamina(player);
                return maximum > 0.0D && StaminaService.getStamina(player) < maximum;
            })
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.stamina_replenishing_pill.effect",
                    Math.round(restoreFraction(data, 0.40D) * 100.0D)
            ))
            .build();


    public static PillItem.Definition cultivation(Identifier path) {
        return PillItem.Definition.builder((level, player, stack, data) -> {
                    OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
                    if (source == null) {
                        return;
                    }

                    PathInstance<?> pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
                    if (pathInstance == null || !pathInstance.canProgress()) {
                        return;
                    }

                    double maximum = Math.max(0.0D, pathInstance.getMaxProgress());
                    double remaining = Math.max(0.0D, maximum - pathInstance.getProgress());
                    double requested = Math.min(remaining, maximum * cultivationFraction(data));
                    if (requested <= 0.0D) return;
                    CultivationUtil.cultivate(player, source, path, pathInstance, null, requested);
                    if (player instanceof ServerPlayer serverPlayer) {
                        double updatedMaximum = pathInstance.getMaxProgress();
                        float progress = updatedMaximum > 0.0D
                                ? (float) Mth.clamp(pathInstance.getProgress() / updatedMaximum, 0.0D, 1.0D)
                                : 0.0F;
                        PacketDistributor.sendToPlayer(serverPlayer,
                                new ClientboundMedicinalCultivationProgressPacket(path, progress));
                    }
                })
                .canConsume((level, player, stack, data) -> {
                    OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
                    if (source == null) {
                        return false;
                    }

                    PathInstance<?> pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
                    return pathInstance != null && pathInstance.canProgress();
                })
                .effectDescription(data -> Component.translatable(
                        "ascension.pill.cultivation.effect",
                        Math.round(cultivationFraction(data) * 100.0D)
                ))
                .build();
    }

    private ModPills() {
    }


    public static ItemStack maximumStack(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(AscensionComponents.PILL_DATA.get(), AscensionComponents.PillData.MAXIMUM);
        return stack;
    }

    private static double cultivationFraction(AscensionComponents.PillData data) {
        AscensionComponents.PillData resolved = data == null ? AscensionComponents.PillData.DEFAULT : data;
        return 0.05D * resolved.strengthMultiplier();
    }

    private static double restoreFraction(AscensionComponents.PillData data, double baseFraction) {
        AscensionComponents.PillData resolved = data == null ? AscensionComponents.PillData.DEFAULT : data;
        return Math.min(1.0D, baseFraction * resolved.strengthMultiplier());
    }
}
