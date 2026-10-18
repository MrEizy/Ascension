package net.zic.ascension.api.ascension.core.skill.castable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import org.jspecify.annotations.Nullable;

/**
 * A skill that can be slotted in the hot bar and cast.
 * <p>
 * While slotted the skill is wrapped in a {@link SkillSlot}, holding its {@link EquipSkillData}.
 * While cast it is wrapped in a {@link CastingSkillInstance}, holding its {@link CastData} and the current phase.
 * A phase is just an id, the skill decides what each phase does and when to move between them
 * with {@link CastingSkillInstance#setPhase}. End the cast with {@link CastingSkillInstance#end}.
 * <p>
 * Casting runs on both the server and the casting client, the server is authoritative and syncs the
 * phase, ticks into the phase and cast data to the client.
 * @param <D> the skill data
 * @param <C> the data of a single cast
 * @param <E> the data kept while slotted
 */
public interface CastableSkill<D extends SkillData, C extends CastData, E extends EquipSkillData> extends Skill<D> {

    //──Equip────────────────────────────────────────────────────────

    E newEquipData(RegistryAccess access);

    SerializerHandler<E> equipSerializerHandler();

    //decode writes into existing data, call newEquipData first
    SyncHandler<E> equipSyncHandler();

    //called when added to/removed from a hot bar slot
    default void onEquip(SkillSlot<D, E> slot) {}

    default void onUnequip(SkillSlot<D, E> slot) {}

    /*
        called when the slot is selected/unselected, on both server and client so can be used for:
        buffs (if you want some special things to happen)
        initializing/cleaning up HUD elements
        initializing/cleaning up keybinds (you might want them to only be available while selected)
     */
    default void onSelected(SkillSlot<D, E> slot) {}

    default void onUnselected(SkillSlot<D, E> slot) {}

    //──Cast────────────────────────────────────────────────────────

    C newCastData(RegistryAccess access);

    //decode writes into existing data, call newCastData first
    SyncHandler<C> castSyncHandler();

    /**
     * ran on both client and server, determines if the skill can be cast (cooldown, resources, targets)
     * @return if it should be cast or not
     */
    CastResult tryCast(CastingSkillInstance<D, C, E> instance);

    /**
     * called once the cast is accepted. set the first phase here, or end the cast for an instant skill
     */
    void startCast(CastingSkillInstance<D, C, E> instance);

    /**
     * called every tick while casting, {@link CastingSkillInstance#getPhaseTicks()} is the number of ticks
     * already completed in the current phase
     */
    void tickCast(CastingSkillInstance<D, C, E> instance);

    /**
     * called once the cast has ended, for any reason ({@link CastingSkillInstance#getStatus()}).
     * charge skills release here, others clean up or apply cooldowns
     */
    void endCast(CastingSkillInstance<D, C, E> instance);

    //called on the server when the skill changes the phase
    default void onPhaseChanged(CastingSkillInstance<D, C, E> instance, @Nullable Identifier previousPhase) {}

    //called on the server when the caster takes damage while casting, end the cast to interrupt it
    default void onDamaged(CastingSkillInstance<D, C, E> instance, double damage) {}

    //visual state shown to nearby players, null for none
    default @Nullable ActiveCastVisualState visualState(CastingSkillInstance<D, C, E> instance) {
        return null;
    }
}
