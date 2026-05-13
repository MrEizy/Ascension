package net.zic.ascension.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;
import java.util.UUID;

public record SealedEntityData(
        EntityType<?> entityType,
        CompoundTag entityData,
        Optional<UUID> uniqueId,
        String customName,
        long timestamp
) {
    public static final Codec<SealedEntityData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(SealedEntityData::entityType),
                    CompoundTag.CODEC.fieldOf("data").forGetter(SealedEntityData::entityData),
                    UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(SealedEntityData::uniqueId),
                    Codec.STRING.fieldOf("name").forGetter(SealedEntityData::customName),
                    Codec.LONG.fieldOf("timestamp").forGetter(SealedEntityData::timestamp)
            ).apply(instance, SealedEntityData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SealedEntityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ENTITY_TYPE),
            SealedEntityData::entityType,
            ByteBufCodecs.COMPOUND_TAG,
            SealedEntityData::entityData,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            SealedEntityData::uniqueId,
            ByteBufCodecs.STRING_UTF8,
            SealedEntityData::customName,
            ByteBufCodecs.VAR_LONG,
            SealedEntityData::timestamp,
            SealedEntityData::new
    );

    public ResourceLocation getEntityId() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
    }

    public boolean isEmpty() {
        return entityType == null;
    }
}