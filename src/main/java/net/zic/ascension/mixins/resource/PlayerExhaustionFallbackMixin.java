package net.zic.ascension.mixins.resource;

import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerExhaustionFallbackMixin {
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void ascension$applyUnclassifiedExhaustion(float amount, CallbackInfo callback) {
        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) {
            return;
        }
        ResourceTransactionService.Result result = ResourceTransactionService.accumulate(
                player,
                AscensionResourceTypes.EXHAUSTION.getId(),
                AscensionResourceSources.UNCLASSIFIED,
                amount
        );
        if (result.status() != ResourceTransactionService.Status.UNSUPPORTED
                && result.status() != ResourceTransactionService.Status.INVALID) {
            callback.cancel();
        }
    }
}
