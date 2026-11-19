package net.zic.ascension.mixins.concealment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.zic.ascension.impl.core.skill.concealment.ConcealmentService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathMessageMixin {
    @ModifyExpressionValue(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"))
    private Component ascension$constrainDeathMessage(Component original, DamageSource source) {
        return ConcealmentService.concealDeathMessage(original, (ServerPlayer) (Object) this, source);
    }
}
