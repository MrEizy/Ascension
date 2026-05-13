package net.zic.ascension.refactor_packages.alchemy.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.refactor_packages.alchemy.BasicPillEffect;

import java.util.ArrayList;

public class MobEffectPillEffect extends BasicPillEffect {


    private final ArrayList<MobEffectInstance> effects = new ArrayList<>();

    public MobEffectPillEffect(Component name,Component description){
        super(name,description);
    }
    public MobEffectPillEffect addEffect(MobEffectInstance instance){
        effects.add(instance);
        return this;
    }




    @Override
    public boolean tryConsume(LivingEntity entity, ItemStack itemStack, double purityScale, double realmMultiplier) {
        for (MobEffectInstance e : effects) entity.addEffect(new MobEffectInstance(e));
        return true;
    }
}
