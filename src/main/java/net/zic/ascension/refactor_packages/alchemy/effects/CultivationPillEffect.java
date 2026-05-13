package net.zic.ascension.refactor_packages.alchemy.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.alchemy.BasicPillEffect;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.util.CultivationUtil;

import java.util.List;

public class CultivationPillEffect extends BasicPillEffect {

    private final double amount;
    private final ResourceLocation path;

    public CultivationPillEffect(double amount, ResourceLocation path, Component name, Component description) {
        super(name,description);
        this.amount = amount;
        this.path = path;

    }

    @Override
    public boolean tryConsume(LivingEntity entity, ItemStack itemStack, double purityScale, double realmMultiplier) {
        double amt = amount * purityScale * realmMultiplier;
        IEntityData entityData = entity.getData(ModAttachments.ENTITY_DATA);
        if(!entityData.hasPath(path)) return false; //add some sort of effect?

        return CultivationUtil.tryCultivate(entity,path, List.of(),amt);
        //player.getCooldowns().addCooldown(this, cooldown); //TODO handle in pill item
        //if (!player.getAbilities().instabuild) itemStack.shrink(1); TODO handle in item
    }

}
