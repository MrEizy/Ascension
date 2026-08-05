package net.zic.ascension.mixins.resource;

import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import net.zic.ascension.handler.RecoveryTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;

@Mixin(ServerPlayer.class)
public abstract class PlayerJumpExhaustionMixin {
    @Redirect(
            method = "jumpFromGround",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"
            )
    )
    private void ascension$applyJumpExhaustion(ServerPlayer player, float amount) {
        RecoveryTicker.spendJumpStamina(player);
        ResourceTransactionService.accumulate(
                player,
                AscensionResourceTypes.EXHAUSTION.getId(),
                AscensionResourceSources.JUMPING,
                amount
        );
    }
}
