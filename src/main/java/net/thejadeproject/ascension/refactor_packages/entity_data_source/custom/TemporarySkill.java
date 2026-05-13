package net.thejadeproject.ascension.refactor_packages.entity_data_source.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSource;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSourceContainer;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.skills.ISkill;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

public class TemporarySkill implements IEntityDataSource {
    @Override
    public void onAdded(IEntityData entityData,IEntityDataSourceContainer container) {
        if(container instanceof TemporarySkillContainer temporarySkillContainer){
            entityData.giveSkill(temporarySkillContainer.getSkill(), ModForms.MORTAL_VESSEL.getId());
        }
    }

    @Override
    public void onRemoved(IEntityData entityData, IEntityDataSourceContainer container) {
        if(container instanceof TemporarySkillContainer temporarySkillContainer){
            entityData.removeSkill(temporarySkillContainer.getSkill(), ModForms.MORTAL_VESSEL.getId());
        }
    }

    public static TemporarySkillContainer create(ResourceLocation skill,int totalTicks,ResourceLocation identifier){
        return new TemporarySkillContainer(skill,totalTicks,identifier);
    }
    public void tick(IEntityData entityData,IEntityDataSourceContainer container){
        if(!(container instanceof TemporarySkillContainer temporarySkillContainer)) return;

        temporarySkillContainer.setTicksLeft(temporarySkillContainer.getTicksLeft()-1);
        if(temporarySkillContainer.getTicksLeft() <= 0){
            entityData.removeEntitySource(container.getInstanceIdentifier());
        }
    }
    @Override
    public IEntityDataSourceContainer fromCompound(CompoundTag tag) {
        ResourceLocation skill = ResourceLocation.parse(tag.getString("skill"));
        int totalTicks = tag.getInt("total_ticks");
        int ticksLeft = tag.getInt("ticks_left");
        TemporarySkillContainer container = new TemporarySkillContainer(skill,totalTicks,ResourceLocation.parse(tag.getString("instance_identifier")));
        container.setTicksLeft(ticksLeft);
        return container;
    }

    @Override
    public IEntityDataSourceContainer fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation skill = ByteBufUtil.readResourceLocation(buf);
        int totalTicks = buf.readInt();
        int ticksLeft = buf.readInt();
        TemporarySkillContainer container = new TemporarySkillContainer(skill,totalTicks,ByteBufUtil.readResourceLocation(buf));
        container.setTicksLeft(ticksLeft);
        return container;
    }
}
