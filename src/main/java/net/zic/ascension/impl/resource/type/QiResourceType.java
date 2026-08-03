package net.zic.ascension.impl.resource.type;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;

public final class QiResourceType extends AbstractBoundedResourceType {
    @Override
    public boolean supports(LivingEntity entity) {
        return entity != null && entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER) != null;
    }

    @Override
    public double getAmount(LivingEntity entity) {
        EntityQiProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        return provider == null ? 0.0D : provider.getQi();
    }

    @Override
    public double getMaximum(LivingEntity entity) {
        EntityQiProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        return provider == null ? 0.0D : provider.getMaxQi();
    }

    @Override
    protected void setAmount(LivingEntity entity, double amount) {
        EntityQiProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if (provider != null) {
            provider.setQi(amount);
        }
    }
}
