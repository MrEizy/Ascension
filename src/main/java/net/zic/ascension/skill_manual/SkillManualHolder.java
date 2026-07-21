package net.zic.ascension.skill_manual;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.data_source.DataSourceInstance;

public class SkillManualHolder implements DataSourceInstance {



    private final int maxManuals;

    private int level;
    private double progress;

    public SkillManualHolder(int maxManuals){
        this.maxManuals = maxManuals;
    }
    public SkillManualHolder(int maxManuals,int level,double progress){
        this(maxManuals);
        this.level = level;
        this.progress = progress;
    }
    public void skillCast(LivingEntity entity, Identifier skill){

    }

    @Override
    public void write(ValueOutput output) {

    }

    @Override
    public void encode(ByteBuf buf) {

    }
}
