package net.zic.ascension.api.ascension.core.skill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

/**
 * A skill as defined in the skill registry.
 * Holds the configuration and the logic, per-origin state belongs in {@link SkillData}.
 * Skills are always accessed through a {@link SkillInstance}, which pairs the skill with its typed data.
 * @param <D> the data this skill creates and runs on
 */
public interface Skill<D extends SkillData> {

    SkillType getType();

    Component getName();

    Component getDescription();


    /**
     * called when the skill is added to an origin source
     * @param source the origin source it is being added to
     * @param instance the instance being added
     */
    void onAdded(OriginSource source, SkillInstance<D> instance);

    /**
     * Called when the skill is removed from a source
     * @param source the source it is removed from
     * @param instance the instance being removed
     */
    void onRemoved(OriginSource source, SkillInstance<D> instance);

    //called when an entity that owns an origin detects the skill was changed
    void applyToEntity(LivingEntity entity, SkillInstance<D> instance);

    //called when either an entity is detached from an origin or the skill is removed from the origin
    void removeFromEntity(LivingEntity entity, SkillInstance<D> instance);

    //──Data────────────────────────────────────────────────────────

    //creates fresh data, also used as the target when decoding
    D newData(RegistryAccess access);

    //saves and loads the data, read is expected to return new data
    SerializerHandler<D> serializerHandler();

    /**
     * syncs the data to clients. decode writes into existing data, so always call {@link #newData} first and decode into that.
     * <p>
     * Do not use {@link SyncHandler.StreamCodecSyncHandler}, its decode does nothing.
     */
    SyncHandler<D> syncHandler();
}
