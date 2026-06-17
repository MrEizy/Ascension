package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.tribulation.TribulationData;
import net.zic.ascension.api.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.core.tribulation.TribulationInstance;
import net.zic.ascension.api.core.tribulation.TribulationManager;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.api.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.core.tribulation.LightingTribulationData;
import net.zic.ascension.impl.core.tribulation.LightningTribulationDefinition;
import net.zic.ascension.impl.datapack.skill.castable.DebugCastableType;
import net.zic.ascension.impl.datapack.skill.castable.cultivation.SimpleCultivationSkillType;
import net.zic.ascension.impl.datapack.skill.passive.SimplePassiveSkillType;

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
        return RecordCodecBuilder.<LightingTribulationData>mapCodec(
                instance->instance.group(
                        Codec.INT.fieldOf("strikes").forGetter(LightingTribulationData::getLightningSurvived),
                        Codec.STRING.xmap(UUID::fromString,UUID::toString).optionalFieldOf("id",UUID.randomUUID()).forGetter(LightingTribulationData::getUUID)
                ).apply(instance,LightingTribulationData::new)
        );
    }


    @Override
    public TribulationData newData(TribulationDefinition  definition) {
        return new LightingTribulationData(0,UUID.randomUUID());
    }

    @Override
    public void onAdded(OriginSource source, TribulationData tribulationData) {

    }

    @Override
    public void onRemoved(OriginSource source, TribulationData tribulationData) {

    }

    @Override
    public void tick(TribulationManager manager, UUID uuid, TribulationInstance instance) {
        if(!instance.isEntityLoaded()) return;
        if(!(instance.getData() instanceof LightingTribulationData data)) return;
        if(!(instance.getTribulation() instanceof LightningTribulationDefinition definition)) return;

        if(data.getLightningSurvived() == definition.lightingStrikes()){

            manager.finishTribulation(uuid);

            System.out.println("entity survived");
            //TODO call conusmer if present
            if(instance.getFinalizationConsumer() != null)instance.getFinalizationConsumer().accept(instance.getData());
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
