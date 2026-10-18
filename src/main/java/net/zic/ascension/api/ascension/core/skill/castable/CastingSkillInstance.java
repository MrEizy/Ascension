package net.zic.ascension.api.ascension.core.skill.castable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.event.skill.SkillCastEvent;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A skill being cast.
 * Wraps the skill, its cast data and the equip data of the slot it was cast from, and defers calls to the skill
 * so it always receives its own typed data.
 * <p>
 * Also holds the lifecycle of the cast: the current phase, how many ticks the cast is into that phase, and the
 * status. A phase is just an id, the skill decides what it means and when to change it.
 */
public final class CastingSkillInstance<D extends SkillData, C extends CastData, E extends EquipSkillData> {
    //the input that keeps a cast going
    public static final Identifier CAST_INPUT = AscensionCraft.prefix("skill_cast");

    private final LivingEntity caster;
    private final Identifier skillId;
    private final CastableSkill<D, C, E> skill;
    private final E equipData;
    private C castData;

    private @Nullable Identifier phase;
    private int phaseTicks;
    private int totalTicks;
    private boolean phaseChangedThisTick;
    private final CastStatus status = new CastStatus();

    //sync state, the phase or cast data changed since the last sync
    private boolean dirty = true;

    private CastingSkillInstance(LivingEntity caster, Identifier skillId, CastableSkill<D, C, E> skill, E equipData, C castData) {
        this.caster = caster;
        this.skillId = skillId;
        this.skill = skill;
        this.equipData = equipData;
        this.castData = castData;
    }

    static <D extends SkillData, C extends CastData, E extends EquipSkillData> CastingSkillInstance<D, C, E> create(
            LivingEntity caster, Identifier skillId, CastableSkill<D, C, E> skill, E equipData, RegistryAccess access
    ) {
        return new CastingSkillInstance<>(caster, skillId, skill, equipData, skill.newCastData(access));
    }

    //creates a cast that is not tied to a slot, it receives fresh equip data
    public static @Nullable CastingSkillInstance<?, ?, ?> create(LivingEntity caster, Identifier skillId, RegistryAccess access) {
        CastableSkill<?, ?, ?> skill = CastableSkillLookup.castable(skillId, access);
        return skill == null ? null : createUnslotted(caster, skillId, skill, access);
    }

    private static <D extends SkillData, C extends CastData, E extends EquipSkillData> CastingSkillInstance<D, C, E> createUnslotted(
            LivingEntity caster, Identifier skillId, CastableSkill<D, C, E> skill, RegistryAccess access
    ) {
        return create(caster, skillId, skill, skill.newEquipData(access), access);
    }

    //──Getters────────────────────────────────────────────────────────
    public LivingEntity getCaster() {return caster;}
    public Identifier getSkillId() {return skillId;}
    public CastableSkill<D, C, E> getSkill() {return skill;}
    public C getCastData() {return castData;}
    public E getEquipData() {return equipData;}
    public @Nullable Identifier getPhase() {return phase;}
    //ticks completed in the current phase
    public int getPhaseTicks() {return phaseTicks;}
    //ticks completed since the cast started
    public int getTotalTicks() {return totalTicks;}
    public CastStatus getStatus() {return status;}

    public boolean isCasting() {
        return status.isCasting();
    }

    public boolean isPhase(Identifier phase) {
        return phase.equals(this.phase);
    }

    //the caster's skill data for this skill, null if the caster does not have the skill
    public @Nullable D getSkillData() {
        return CastableSkillLookup.skillData(caster, skillId, skill);
    }

    //call after changing the skill data, syncs it
    public void markSkillDataDirty() {
        CastableSkillLookup.markSkillDataDirty(caster, skillId);
    }

    //call after changing the cast data, syncs it
    public void markCastDataDirty() {
        dirty = true;
    }

    /**
     * whether the caster is still holding the cast input. players hold it with the cast key,
     * other entities (bosses, mobs) have their AI start/end the action on their action manager
     */
    public boolean isInputHeld() {
        return caster.getData(ZenithAttachments.ACTION_MANAGER).isActive(CAST_INPUT);
    }

    //──Lifecycle────────────────────────────────────────────────────────

    /**
     * moves the cast to a new phase, resetting the phase ticks
     */
    public void setPhase(Identifier phase) {
        if (phase.equals(this.phase)) return;
        Identifier previous = this.phase;
        this.phase = phase;
        phaseTicks = 0;
        phaseChangedThisTick = true;
        dirty = true;
        if (!caster.level().isClientSide()) {
            skill.onPhaseChanged(this, previous);
            NeoForge.EVENT_BUS.post(new SkillCastEvent.PhaseChanged(this, previous));
        }
    }

    //ends the cast, the first reason given is kept
    public void end(CastStatus.Reason reason) {
        if (isCasting()) status.setReason(reason == null ? CastStatus.Reason.CANCELLED : reason);
    }

    //──Dispatch────────────────────────────────────────────────────────
    //called by the cast handler

    public CastResult tryCast() {
        return skill.tryCast(this);
    }

    public void start() {
        skill.startCast(this);
    }

    //runs the skill for one tick, the tick only counts while the cast continues
    public void tick() {
        phaseChangedThisTick = false;
        skill.tickCast(this);
        if (!isCasting()) return;
        totalTicks++;
        if (!phaseChangedThisTick) phaseTicks++;
    }

    //advances the ticks without running the skill, used by clients that only display the cast
    public void advance() {
        if (!isCasting()) return;
        totalTicks++;
        phaseTicks++;
    }

    public void finish() {
        if (isCasting()) status.cancel();
        skill.endCast(this);
    }

    public void onDamaged(double damage) {
        skill.onDamaged(this, damage);
    }

    public @Nullable ActiveCastVisualState visualState() {
        return skill.visualState(this);
    }

    //──Sync────────────────────────────────────────────────────────

    public boolean isDirty() {return dirty;}
    public void resolveDirty() {dirty = false;}

    //the whole cast is length prefixed so a skill the client does not know can be skipped
    public void encode(ByteBuf buf, RegistryAccess access) {
        ByteBufHelpers.encodeIdentifier(skillId, buf);
        ByteBuf castBuf = Unpooled.buffer();
        castBuf.writeBoolean(phase != null);
        if (phase != null) ByteBufHelpers.encodeIdentifier(phase, castBuf);
        castBuf.writeInt(phaseTicks);
        castBuf.writeInt(totalTicks);
        skill.castSyncHandler().encode(castData, castBuf, access);
        buf.writeInt(castBuf.readableBytes());
        buf.writeBytes(castBuf);
    }

    public static Identifier decodeSkillId(ByteBuf buf) {
        return ByteBufHelpers.decodeIdentifier(buf);
    }

    /**
     * reads the rest of a synced cast, after the id, into this instance. no skill logic or events run
     * @param tickTolerance when the phase did not change, local ticks within this many ticks of the server are kept,
     *                      so a predicting client does not jitter. 0 always takes the server's ticks
     */
    public void decodeState(ByteBuf buf, RegistryAccess access, int tickTolerance) {
        ByteBuf castBuf = buf.readSlice(buf.readInt());
        Identifier syncedPhase = castBuf.readBoolean() ? ByteBufHelpers.decodeIdentifier(castBuf) : null;
        int syncedPhaseTicks = Math.max(0, castBuf.readInt());
        int syncedTotalTicks = Math.max(0, castBuf.readInt());
        boolean keepLocalTicks = tickTolerance > 0
                && Objects.equals(syncedPhase, phase)
                && Math.abs(syncedPhaseTicks - phaseTicks) <= tickTolerance;
        phase = syncedPhase;
        if (!keepLocalTicks) {
            phaseTicks = syncedPhaseTicks;
            totalTicks = syncedTotalTicks;
        }
        C decoded = skill.newCastData(access);
        skill.castSyncHandler().decode(decoded, castBuf, access);
        castData = decoded;
        dirty = false;
    }

    //skips a synced cast for a skill this side does not know
    public static void skipState(ByteBuf buf) {
        buf.skipBytes(buf.readInt());
    }
}
