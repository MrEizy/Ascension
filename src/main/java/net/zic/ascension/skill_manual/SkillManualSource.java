package net.zic.ascension.skill_manual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.data_source.DataSource;
import net.zic.ascension.api.core.data_source.DataSourceInstance;
import net.zic.ascension.api.core.data_source.LoadOrder;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.data_source.DataSourceType;
import net.zic.ascension.impl.datapack.data_source.AscensionDataSources;

public class SkillManualSource implements DataSource {


    private final int MAX_MANUALS;

    public SkillManualSource(int maxManuals){
        MAX_MANUALS = maxManuals;
        SkillManualLevelingHandler.addHolder(this);
    }

    public static class Type extends DataSourceType{

        @Override
        public MapCodec<? extends DataSource> codec() {
            return RecordCodecBuilder.<SkillManualSource>mapCodec(
                    instance->instance.group(
                            Codec.INT.fieldOf("max_manuals").forGetter(SkillManualSource::getMaxManuals)
                    ).apply(instance,SkillManualSource::new)
            );
        }
    }



    public int getMaxManuals(){
        return MAX_MANUALS;
    }

    @Override
    public LoadOrder getLoadOrder() {
        return LoadOrder.FINAL;
    }

    @Override
    public DataSourceType getType() {
        return AscensionDataSources.SKILL_MANUAL_HOLDER_SOURCE_TYPE.get();
    }

    @Override
    public void onAdded(OriginSource source, DataSourceInstance data) {
        //TODO
    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance data) {
        //TODO
    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance data) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance data) {

    }

    @Override
    public DataSourceInstance newInstance(RegistryAccess access) {
        return new SkillManualHolder(getMaxManuals());
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        return new SkillManualHolder(getMaxManuals(),input.getIntOr("level",0),input.getDoubleOr("progress",0));
    }

    @Override
    public DataSourceInstance loadInstance(ByteBuf buf) {
        return new SkillManualHolder(getMaxManuals(),buf.readInt(),buf.readDouble());

    }
}
