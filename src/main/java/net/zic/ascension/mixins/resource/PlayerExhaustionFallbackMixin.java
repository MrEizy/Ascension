package net.zic.ascension.mixins.resource;

import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.core.resource.ResourceTransactionResult;
import net.zic.ascension.api.core.resource.ResourceTransactions;
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
        ResourceTransactionResult result = ResourceTransactions.accumulate(
                player,
                AscensionResourceTypes.EXHAUSTION.getId(),
                AscensionResourceSources.UNCLASSIFIED,
                amount
        );
        if (result.status() != net.zic.ascension.api.core.resource.ResourceTransactionStatus.UNSUPPORTED
                && result.status() != net.zic.ascension.api.core.resource.ResourceTransactionStatus.INVALID) {
            callback.cancel();
        }
    }
}
