package net.zic.ascension.mixins.resource;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.zic.ascension.api.core.resource.ResourceTransactions;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
public abstract class FoodDataRegenerationExhaustionMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"
            )
    )
    private void ascension$applyRegenerationExhaustion(
            FoodData foodData,
            float amount,
            ServerPlayer player
    ) {
        ResourceTransactions.accumulate(
                player,
                AscensionResourceTypes.EXHAUSTION.getId(),
                AscensionResourceSources.NATURAL_REGENERATION,
                amount
        );
    }
}