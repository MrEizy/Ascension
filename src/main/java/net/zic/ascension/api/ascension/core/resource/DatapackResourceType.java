package net.zic.ascension.api.ascension.core.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.CoreAttachments;

public record DatapackResourceType(Identifier id, ResourceDefinition definition) implements ResourceType {
    @Override
    public boolean supports(LivingEntity entity) {
        return entity != null && definition != null;
    }

    @Override
    public double getAmount(LivingEntity entity) {
        DatapackResourceData data = entity.getData(CoreAttachments.DATAPACK_RESOURCES);
        return data.contains(id) ? Math.clamp(data.get(id), 0.0D, getMaximum(entity)) : definition.starting(entity, id);
    }

    @Override
    public double getMaximum(LivingEntity entity) {
        return definition.maximum(entity, id);
    }

    @Override
    public void setAmount(LivingEntity entity, double amount) {
        entity.getData(CoreAttachments.DATAPACK_RESOURCES).set(id, Math.clamp(amount, 0.0D, getMaximum(entity)));
    }
}
