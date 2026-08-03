package net.zic.ascension.capabilities.qi_provider;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactions;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;

public class SimpleEntityQiProvider implements EntityQiProvider {
    private final LivingEntity attachedEntity;

    public SimpleEntityQiProvider(LivingEntity attachedEntity) {
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
        ResourceTransactions.restore(
                attachedEntity,
                AscensionResourceTypes.QI.getId(),
                AscensionResourceSources.DIRECT,
                amount
        );
    }

    @Override
    public boolean reduceQi(double amount) {
        return ResourceTransactions.consume(
                attachedEntity,
                AscensionResourceTypes.QI.getId(),
                AscensionResourceSources.DIRECT,
                amount
        ).succeeded();
    }

    @Override
    public void setQi(double value) {
        attachedEntity.setData(
                AscensionAttachments.ENTITY_QI,
                Math.clamp(value, 0.0D, getMaxQi())
        );
    }
}
