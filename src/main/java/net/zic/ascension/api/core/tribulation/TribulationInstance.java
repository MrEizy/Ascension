package net.zic.ascension.api.core.tribulation;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.tribulation.TribulationType;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class TribulationInstance {
    private final TribulationDefinition definition;
    private final TribulationData data;
    private final UUID entityId;
    private LivingEntity entityReference;
    private BlockPos lastKnownBlockBos;
    private Consumer<TribulationData> finalizationConsumer;
    private final static Consumer<TribulationData> EMPTY_CONSUMER = (data)->{};
    public final static TribulationInstance INVALID = new TribulationInstance(null,null,null,null);

    public TribulationInstance(TribulationDefinition definition,TribulationData data,LivingEntity entity){
        this.data = data;
        this.definition = definition;
        this.entityId = entity.getUUID();
        this.entityReference = entity;
    }
    public TribulationInstance(TribulationDefinition definition,UUID entityId,BlockPos lastPos,TribulationData data){
        this.definition = definition;
        this.data = data;
        this.entityId = entityId;
        this.lastKnownBlockBos = lastPos;
    }
    public TribulationDefinition getTribulation(){return definition;}
    public TribulationData getData(){
        return data;
    }
    public UUID getEntityId(){
        return entityId;
    }

    public LivingEntity getEntity(){
        return entityReference;
    }

    public boolean isEntityLoaded(){
        return entityReference != null;
    }

    public void setEntityReference(LivingEntity entity){
        if(entity == null && !isEntityLoaded()) return;
        if(entity == null) lastKnownBlockBos = entityReference.blockPosition();

        entityReference = entity;
    }

    public void setFinalizationConsumer(Consumer<TribulationData> consumer){
        this.finalizationConsumer =consumer;
    }
    public Consumer<TribulationData> getFinalizationConsumer(){
        return finalizationConsumer == null ? EMPTY_CONSUMER : finalizationConsumer;
    }

    public BlockPos getPosition(){
        return entityReference == null ? lastKnownBlockBos : entityReference.blockPosition();
    }



    public static final Codec<TribulationInstance> RAW_CODEC = RecordCodecBuilder.<TribulationInstance>create(
            instance->
                    instance.group(
                        TribulationType.TRIBULATION_CODEC.fieldOf("definition").forGetter(TribulationInstance::getTribulation),
                        Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("entity").forGetter(TribulationInstance::getEntityId),
                        BlockPos.CODEC.optionalFieldOf("last_post",new BlockPos(0,0,0)).forGetter(TribulationInstance::getPosition),
                        TribulationType.TRIBULATION_DATA_CODEC.fieldOf("data").forGetter(TribulationInstance::getData)
                    ).apply(instance,TribulationInstance::new)
    );


    public static final Codec<TribulationInstance> CODEC = Codec.of(
            RAW_CODEC,
            new Decoder<TribulationInstance>() {
                @Override
                public <T> DataResult<Pair<TribulationInstance, T>> decode(DynamicOps<T> ops, T input) {
                    DataResult<Pair<TribulationInstance,T>> result = RAW_CODEC.decode(ops, input);
                    if(result.isError()){
                        return DataResult.success(Pair.of(INVALID, ops.empty()));
                    }
                    return result;


                }
            });
}
