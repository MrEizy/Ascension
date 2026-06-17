package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.tribulation.TribulationData;
import net.zic.ascension.api.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.api.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.core.tribulation.LightingTribulationData;
import net.zic.ascension.impl.core.tribulation.LightningTribulationDefinition;
import net.zic.ascension.impl.datapack.skill.castable.DebugCastableType;
import net.zic.ascension.impl.datapack.skill.castable.cultivation.SimpleCultivationSkillType;
import net.zic.ascension.impl.datapack.skill.passive.SimplePassiveSkillType;

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
                        Codec.INT.fieldOf("strikes").forGetter(LightingTribulationData::getLightningSurvived)
                ).apply(instance,LightingTribulationData::new)
        );
    }

    @Override
    public TribulationData newData() {
        return new LightingTribulationData(0);
    }

    @Override
    public void onAdded(OriginSource source, TribulationData tribulationData) {

    }

    @Override
    public void onRemoved(OriginSource source, TribulationData tribulationData) {

    }


}
