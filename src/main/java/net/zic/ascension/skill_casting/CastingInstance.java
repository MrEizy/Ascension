package net.zic.ascension.skill_casting;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.castable.CastData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.PreCastData;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastType;
import net.zic.ascension.api.core.skill.castable.held.HeldCastData;
import net.zic.ascension.api.core.skill.castable.held.HeldCastVisualState;
import net.zic.ascension.impl.core.skill.castable.held.HeldCastSkill;
import net.zic.zenithlib.network.ByteBufHelpers;

public class CastingInstance {
    private Identifier skill;
    private final CastStatus status = new CastStatus();
    private CastData castData;
    private int ticksElapsed;
    private boolean dirty;

    public Identifier getSkill() {
        return skill;
    }

    public int getTicksElapsed() {
        return ticksElapsed;
    }

    public CastData getCastData() {
        return castData;
    }

    public HeldCastVisualState getHeldVisualState(Player caster) {
        if (skill == null || !(castData instanceof HeldCastData heldData)) {
            return null;
        }
        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                caster.level().registryAccess()
        ) instanceof HeldCastSkill heldSkill)) {
            return null;
        }
        return heldSkill.visualState(caster, heldData);
    }

    public void startCast(Player caster, Identifier skill, PreCastData preCastData) {
        if (skill != null && skill.equals(this.skill)) {
            return;
        }

        endCast(caster, CastStatus.Reason.CANCELLED);
        status.resolve();
        if (skill == null) {
            return;
        }

        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                caster.level().registryAccess()
        ) instanceof CastableSkill castableSkill)) {
            return;
        }

        this.skill = skill;
        CastResult result = castableSkill.tryCast(caster);
        if (!result.isSuccess()) {
            if (result.message != null) {
                caster.sendOverlayMessage(result.message);
            }
            this.skill = null;
            markDirty();
            return;
        }

        castData = castableSkill.initialCast(caster, preCastData);
        NeoForge.EVENT_BUS.post(new SkillCastEvent(caster, skill, preCastData));
        if (castableSkill.getCastType() == CastType.INSTANT) {
            this.skill = null;
            castData = null;
            markDirty();
            return;
        }

        ticksElapsed = 0;
        markDirty();
    }

    public void endCast(Player caster, CastStatus.Reason reason) {
        if (skill == null) {
            return;
        }
        status.setReason(reason == null ? CastStatus.Reason.CANCELLED : reason);
        endCast(caster);
    }

    public void endCast(Player caster) {
        if (skill == null) {
            return;
        }

        if (status.isCasting()) {
            status.cancel();
        }
        if (CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                caster.level().registryAccess()
        ) instanceof CastableSkill castableSkill) {
            castableSkill.finalCast(caster, status, castData, ticksElapsed);
        }

        skill = null;
        castData = null;
        status.resolve();
        ticksElapsed = 0;
        markDirty();
    }

    public void continueCasting(Player caster) {
        if (skill == null) {
            return;
        }
        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                caster.level().registryAccess()
        ) instanceof CastableSkill castableSkill)) {
            endCast(caster, CastStatus.Reason.INVALIDATED);
            return;
        }

        castableSkill.continueCasting(caster, status, castData, ticksElapsed);
        if (castData != null && castData.isDirty()) {
            markDirty();
        }
        if (!status.isCasting()) {
            endCast(caster);
            return;
        }
        ticksElapsed++;
    }

    public void recordDamage(Player caster, double damage) {
        if (skill == null || !(castData instanceof HeldCastData heldData)) {
            return;
        }
        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                caster.level().registryAccess()
        ) instanceof HeldCastSkill heldSkill)) {
            return;
        }
        if (heldSkill.shouldInterrupt(caster, heldData, ticksElapsed, damage)) {
            endCast(caster, CastStatus.Reason.INTERRUPTED);
        }
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resolveDirty() {
        dirty = false;
    }

    public void encode(ByteBuf buf) {
        buf.writeBoolean(skill != null);
        if (skill == null) {
            return;
        }
        ByteBufHelpers.encodeIdentifier(skill, buf);
        buf.writeInt(Math.max(0, ticksElapsed));
        buf.writeBoolean(castData != null);
        if (castData != null) {
            castData.encode(buf);
            castData.resolveDirty();
        }
    }

    public void decode(RegistryFriendlyByteBuf buf, Player player) {
        if (!buf.readBoolean()) {
            endCast(player, CastStatus.Reason.INVALIDATED);
            return;
        }

        Identifier decodedSkill = buf.readIdentifier();
        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                decodedSkill,
                buf.registryAccess()
        ) instanceof CastableSkill castableSkill)) {
            endCast(player, CastStatus.Reason.INVALIDATED);
            return;
        }

        if (skill != null && !decodedSkill.equals(skill)) {
            endCast(player, CastStatus.Reason.INVALIDATED);
        }
        skill = decodedSkill;
        ticksElapsed = Math.max(0, buf.readVarInt());
        castData = buf.readBoolean() ? castableSkill.loadCastData(buf) : null;
        resolveDirty();
    }
}
