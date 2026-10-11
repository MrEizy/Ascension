package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Optional component of a TribulationInstance, used when a tribulation tracks an entity
 */
public final class TribulationTarget {
    public static final Codec<TribulationTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("entity").forGetter(TribulationTarget::getEntityId),
            DimensionChangeBehaviour.CODEC.optionalFieldOf("dimension_change", DimensionChangeBehaviour.STAY).forGetter(TribulationTarget::getDimensionChangeBehaviour)
    ).apply(instance, TribulationTarget::new));

    private final UUID entityId;
    private final DimensionChangeBehaviour dimensionChangeBehaviour;
    private @Nullable LivingEntity entity;

    public TribulationTarget(UUID entityId, DimensionChangeBehaviour dimensionChangeBehaviour) {
        this.entityId = entityId;
        this.dimensionChangeBehaviour = dimensionChangeBehaviour;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public DimensionChangeBehaviour getDimensionChangeBehaviour() {
        return dimensionChangeBehaviour;
    }

    public @Nullable LivingEntity getEntity() {
        return entity;
    }

    void setEntity(@Nullable LivingEntity entity) {
        this.entity = entity;
    }
}
