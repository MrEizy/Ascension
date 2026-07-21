package net.zic.ascension.api.ascension.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;

public interface Skill {

    SkillType getType();

    Component getName();

    Component getDescription();


    /**
     * called when the skill is added to an origin source
     * @param source the origin source it is being added to
     * @param data the data for this skill
     */
    void onAdded(OriginSource source, SkillData data);

    /**
     * Called when the skill is removed from a source
     * @param source the source it is removed from
     * @param data the data of this skill
     */
    void onRemoved(OriginSource source, SkillData data);

    //called when an entity that owns an origin detects the skill was changed
    void applyToEntity(LivingEntity entity,SkillData data);

    //called when either an entity is detached from an origin or the skill is removed from the origin
    void removeFromEntity(LivingEntity entity,SkillData data);


    SkillData newData(RegistryAccess access);
    SkillData loadData(ValueInput input,RegistryAccess access);
    SkillData loadData(ByteBuf buf);
}
