package net.zic.ascension.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.data_attachments.ModAttachments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {



    @Inject(method = "getSpeed", at = @At("HEAD"), cancellable = true)
    private void getSpeed(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ModAttachments.ENTITY_DATA)){

            cir.setReturnValue((float)self.getData(ModAttachments.ENTITY_DATA).getAscensionAttributeHolder().getAttribute(Attributes.MOVEMENT_SPEED).getValue());
        }

    }
}
