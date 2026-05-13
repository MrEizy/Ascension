package net.thejadeproject.ascension.refactor_packages.entity_data_source.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSource;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSourceContainer;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.ModDataSources;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

public class SkillSourceContainer implements IEntityDataSourceContainer {
    private final ResourceLocation skill;

    private ResourceLocation instanceIdentifier;

    public SkillSourceContainer(ResourceLocation skill, ResourceLocation instanceIdentifier) {
        this.skill = skill;
        this.instanceIdentifier = instanceIdentifier;

    }

    public ResourceLocation getSkill() {
        return skill;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        ByteBufUtil.encodeString(buf, skill.toString());

        ByteBufUtil.encodeString(buf, instanceIdentifier.toString());
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putString("skill", skill.toString());
        tag.putString("instance_identifier", instanceIdentifier.toString());
    }

    @Override
    public ResourceLocation getInstanceIdentifier() {
        return instanceIdentifier;
    }

    @Override
    public IEntityDataSource getDataSource() {
        return ModDataSources.SKILL_SOURCE.get();
    }
}