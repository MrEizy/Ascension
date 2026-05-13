package net.zic.ascension.refactor_packages.alchemy.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.alchemy.BasicPillEffect;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.qi.EntityQiContainer;

public class QiRestorePillEffect extends BasicPillEffect {

    private final double amount;

    public QiRestorePillEffect(double amount, Component name, Component description) {
        super(name, description);
        this.amount = amount;
    }

    @Override
    public boolean tryConsume(LivingEntity livingEntity, ItemStack itemStack, double purityScale, double realmMultiplier) {
        double amt = amount * purityScale * realmMultiplier;
        IEntityData entityData = livingEntity.getData(ModAttachments.ENTITY_DATA);
        EntityQiContainer qiContainer = entityData.getQiContainer();

        if (qiContainer == null) return false;

        qiContainer.setCurrentQi(qiContainer.getCurrentQi() + amt);
        return true;
    }
}
