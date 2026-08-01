package net.zic.ascension.capabilities.qi_provider;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.util.AscensionAttributes;

public class SimpleEntityQiProvider implements EntityQiProvider {

    private final LivingEntity attachedEntity;

    public SimpleEntityQiProvider(LivingEntity attachedEntity){
        this.attachedEntity = attachedEntity;
    }
    @Override
    public double getQi() {
        return attachedEntity.getData(AscensionAttachments.ENTITY_QI);
    }

    @Override
    public double getMaxQi() {
        return attachedEntity.getAttributeValue(AscensionAttributes.MAX_QI);
    }

    @Override
    public void regenQi(double amount) {
        attachedEntity.setData(AscensionAttachments.ENTITY_QI,Math.clamp(
                getQi()+amount,
                0,
                getMaxQi()
        ));
    }

    @Override
    public boolean reduceQi(double amount) {
        if(amount > getQi()) return false;
        regenQi(-amount);
        return true;
    }

    @Override
    public void setQi(double value) {
        attachedEntity.setData(AscensionAttachments.ENTITY_QI,Math.clamp(
                value,
                0,
                getMaxQi()
        ));
    }
}
