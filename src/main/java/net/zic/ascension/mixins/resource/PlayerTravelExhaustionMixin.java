package net.zic.ascension.mixins.resource;

import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.core.resource.ResourceTransactions;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public abstract class PlayerTravelExhaustionMixin {
    @Redirect(
            method = "checkMovementStatistics",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"
            )
    )
    private void ascension$applyMovementExhaustion(ServerPlayer player, float amount) {
        ResourceTransactions.accumulate(
                player,
                AscensionResourceTypes.EXHAUSTION.getId(),
                AscensionResourceSources.movementSource(player),
                amount
        );
    }
}