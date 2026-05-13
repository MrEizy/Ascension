package net.thejadeproject.ascension.mixins;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getAttributeValue",at=@At("HEAD"),cancellable = true)
    private void getAttributeValue(Holder<Attribute> attributeHolder,CallbackInfoReturnable<Double> cir){
        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ModAttachments.ENTITY_DATA)){
            IEntityData entityData = self.getData(ModAttachments.ENTITY_DATA);
            if(entityData.getAscensionAttributeHolder().getAttribute(attributeHolder) != null){
                cir.setReturnValue(entityData.getAscensionAttributeHolder().getAttribute(attributeHolder).getValue());
            }
        }
    }

    @Inject(method = "getMaxAbsorption",at=@At("HEAD"),cancellable = true)
    private void getMaxAbsorption(CallbackInfoReturnable<Float> cir){


        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ModAttachments.ENTITY_DATA)){
            IEntityData entityData = self.getData(ModAttachments.ENTITY_DATA);
            if(entityData.getAscensionAttributeHolder().getAttribute(Attributes.MAX_ABSORPTION) != null){
                cir.setReturnValue((float)entityData.getAscensionAttributeHolder().getAttribute(Attributes.MAX_ABSORPTION).getValue());
            }
        }
    }
    @Inject(method = "getHealth", at = @At("HEAD"), cancellable = true)
    private void getHealth(CallbackInfoReturnable<Float> cir){
        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ModAttachments.ENTITY_DATA)){

            cir.setReturnValue((float)self.getData(ModAttachments.ENTITY_DATA).getHealth());
        }
    }
    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void getMaxHealth(CallbackInfoReturnable<Float> cir){
        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ModAttachments.ENTITY_DATA)){

            cir.setReturnValue((float)self.getData(ModAttachments.ENTITY_DATA).getAscensionAttributeHolder().getAttribute(Attributes.MAX_HEALTH).getValue());
        }
    }
    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void setHealth(float health, CallbackInfo ci){
        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ModAttachments.ENTITY_DATA)){
            self.getData(ModAttachments.ENTITY_DATA).setHealth(health);
            ci.cancel();
        }
    }
    @Inject(method = "getSpeed", at = @At("HEAD"), cancellable = true)
    private void overrideSpeed(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ModAttachments.ENTITY_DATA)){

            cir.setReturnValue((float)self.getData(ModAttachments.ENTITY_DATA).getAscensionAttributeHolder().getAttribute(Attributes.MOVEMENT_SPEED).getValue());
        }

    }}
