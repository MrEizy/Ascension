package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.core.tribulation.LightningTribulationData;
import net.zic.ascension.impl.core.tribulation.LightningTribulationDefinition;

import java.util.UUID;

public class LightningTribulationType extends TribulationType {

    @Override
    public MapCodec<? extends TribulationDefinition> codec() {
        return RecordCodecBuilder.<LightningTribulationDefinition>mapCodec(
                instance->instance.group(
                        Codec.INT.fieldOf("strikes").forGetter(LightningTribulationDefinition::lightingStrikes),
                        Codec.DOUBLE.fieldOf("damage").forGetter(LightningTribulationDefinition::damage),
                        Codec.INT.fieldOf("tick_delay").forGetter(LightningTribulationDefinition::tickDelay)
                ).apply(instance,LightningTribulationDefinition::new)
        );
    }

    @Override
    public MapCodec<? extends TribulationData> dataCodec() {
        return RecordCodecBuilder.<LightningTribulationData>mapCodec(
                instance->instance.group(
                        Codec.INT.fieldOf("strikes").forGetter(LightningTribulationData::getLightningSurvived),
                        Codec.STRING.xmap(UUID::fromString,UUID::toString).optionalFieldOf("id",UUID.randomUUID()).forGetter(LightningTribulationData::getUUID)
                ).apply(instance, LightningTribulationData::new)
        );
    }


    @Override
    public TribulationData newData(TribulationDefinition  definition) {
        return new LightningTribulationData(0,UUID.randomUUID());
    }

    @Override
    public void onAdded(OriginSource source, TribulationDefinition definition, TribulationData tribulationData) {

    }

    @Override
    public void onRemoved(OriginSource source, TribulationDefinition definition, TribulationData tribulationData) {

    }

    @Override
    public TribulationData validateAndCovert(TribulationDefinition definition, TribulationDefinition oldDefinition, TribulationData oldData) {
        if(oldData instanceof LightningTribulationData lightningData){
            //the data can be converted as long as it extends LightningTribulationData
            LightningTribulationDefinition lightningDefinition = (LightningTribulationDefinition) definition;

            lightningData.setLightningSurvived(Math.min(lightningData.getLightningSurvived(),lightningDefinition.lightingStrikes()));
            return lightningData;
        }else{
            return newData(definition);
        }
    }


    @Override
    public void tick(TribulationManager manager, UUID uuid, TribulationInstance instance) {
        if(!instance.isEntityLoaded()) return;
        if(!(instance.getData() instanceof LightningTribulationData data)) return;
        if(!(instance.getTribulation() instanceof LightningTribulationDefinition definition)) return;

        if(data.getLightningSurvived() == definition.lightingStrikes()){

            manager.finishTribulation(uuid);

            System.out.println("entity survived");
            if(instance.getFinalizationConsumer() != null)instance.getFinalizationConsumer().accept(instance.getTribulation(),instance.getData());
            return;
        }

        if(data.tick(definition.tickDelay())){
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT,instance.getEntity().level());
            bolt.setDamage((float) definition.damage());
            bolt.setPos(instance.getEntity().position());
            instance.getEntity().level().addFreshEntity(bolt);

            data.setLightningSurvived(data.getLightningSurvived()+1);
        }
    }
}
