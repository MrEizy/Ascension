package net.zic.ascension.refactor_packages.alchemy.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.refactor_packages.alchemy.BasicPillEffect;

import java.util.ArrayList;

public class AntidotePillEffect extends BasicPillEffect {

    private final ArrayList<MobEffectInstance> effects = new ArrayList<>();

    public AntidotePillEffect(Component name,Component description){
        super(name,description);
    }
    public AntidotePillEffect addEffect(MobEffectInstance instance){
        effects.add(instance);
        return this;
    }

    @Override
    public boolean shouldGoOnCooldown() {
        return false;
    }

    @Override
    public boolean tryConsume(LivingEntity entity, ItemStack itemStack, double purityScale, double realmMultiplier) {
        for (MobEffectInstance e : effects) entity.removeEffect(e.getEffect());
        return true;
    }
}
