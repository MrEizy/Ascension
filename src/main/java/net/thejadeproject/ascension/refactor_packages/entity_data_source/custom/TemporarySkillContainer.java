package net.thejadeproject.ascension.refactor_packages.entity_data_source.custom;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSource;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSourceContainer;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.ModDataSources;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

import java.util.UUID;

public class TemporarySkillContainer implements IEntityDataSourceContainer {
    private final ResourceLocation skill;
    private final int totalTicks ;
    private int ticksLeft;
    private ResourceLocation instanceIdentifier;

    public TemporarySkillContainer(ResourceLocation skill, int totalTicks,ResourceLocation instanceIdentifier) {
        this.skill = skill;
        this.totalTicks = totalTicks;
        ticksLeft = totalTicks;
        this.instanceIdentifier = instanceIdentifier;
    }
    public void setTicksLeft(int amt){
        ticksLeft=amt;
    }
    public int getTicksLeft(){
        return ticksLeft;
    }
    public int getTotalTicks(){
        return totalTicks;
    }
    public ResourceLocation getSkill(){
        return skill;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        ByteBufUtil.encodeString(buf,skill.toString());
        buf.writeInt(totalTicks);
        buf.writeInt(totalTicks);
        ByteBufUtil.encodeString(buf,instanceIdentifier.toString());
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putString("skill",skill.toString());
        tag.putInt("total_ticks",totalTicks);
        tag.putInt("ticks_left",ticksLeft);
        tag.putString("instance_identifier",instanceIdentifier.toString());
    }

    @Override
    public ResourceLocation getInstanceIdentifier() {
        return instanceIdentifier;
    }

    @Override
    public IEntityDataSource getDataSource() {
        return ModDataSources.TEMP_SKILL_SOURCE.get();
    }


}
