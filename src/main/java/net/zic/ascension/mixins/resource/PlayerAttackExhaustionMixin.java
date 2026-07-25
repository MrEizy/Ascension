package net.zic.ascension.mixins.resource;

import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.core.resource.ResourceTransactions;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerAttackExhaustionMixin {
    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"
            ),
            require = 0
    )
    private void ascension$applyAttackExhaustion(Player player, float amount) {
        ResourceTransactions.accumulate(
                player,
                AscensionResourceTypes.EXHAUSTION.getId(),
                AscensionResourceSources.ATTACKING,
                amount
        );
    }
}
