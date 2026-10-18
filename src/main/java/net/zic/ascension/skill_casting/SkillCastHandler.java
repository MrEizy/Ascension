package net.zic.ascension.skill_casting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.SkillSlot;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.hotbar.SkillHotBar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * a player's hot bar and cast input. synced only to the owning player.
 * the cast itself lives on the player's {@link SkillCaster}, like any other entity.
 * changes only mark the hot bar dirty, it syncs once per tick from {@link #tick()} or when a packet handler resolves
 */
public class SkillCastHandler {
    private final Player player;
    private boolean waitingForCastRelease;
    private final SkillHotBar hotBar;

    public SkillCastHandler(Player player) {
        this.player = player;
        this.hotBar = new SkillHotBar(player);
    }

    public Player getPlayer() {
        return player;
    }

    public SkillCaster getCaster() {
        return SkillCaster.of(player);
    }

    public void resolve() {
        if (player.level().isClientSide()) {
            return;
        }
        if (hotBar.isDirty()) {
            player.syncData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
            //cleared here rather than in write, write is skipped when the player can not be sent to
            hotBar.resolveDirty();
        }
    }

    //──Hot Bar────────────────────────────────────────────────────────

    public int getMaxSlots() {
        return hotBar.getMaxSlots();
    }

    public int getSelectedSlot() {
        return hotBar.getSelectedSlot();
    }

    public @Nullable Identifier getSkill(int slot) {
        return hotBar.getSkill(slot);
    }

    public @Nullable SkillSlot<?, ?> getSlot(int slot) {
        return hotBar.getSlot(slot);
    }

    public @Nullable SkillSlot<?, ?> findSlot(Identifier skill) {
        return hotBar.findSlot(skill);
    }

    public void markHotBarDirty() {
        hotBar.markDirty();
    }

    public void slotSkill(@Nullable Identifier skill, int slot) {
        if (slot < 0 || slot >= getMaxSlots()) {
            return;
        }
        hotBar.slotSkill(skill, slot);
    }

    //called on both sides, the client selects right away so a cast straight after uses the new slot
    public void select(int slot) {
        if (slot < 0 || slot >= getMaxSlots()) {
            return;
        }
        hotBar.select(slot);
    }

    //clears a skill the player no longer has from the hot bar, and ends its cast
    public void removeSkill(Identifier skill) {
        if (skill == null) {
            return;
        }
        SkillCaster caster = SkillCaster.getExisting(player);
        if (caster != null && skill.equals(caster.getCastingSkill())) {
            caster.invalidateCast();
        }
        for (int slot = 0; slot < getMaxSlots(); slot++) {
            if (skill.equals(hotBar.getSkill(slot))) {
                hotBar.slotSkill(null, slot);
            }
        }
    }

    //clears every slotted skill the player no longer has
    public void removeUnownedSkills() {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return;
        }
        for (int slot = 0; slot < getMaxSlots(); slot++) {
            Identifier skill = hotBar.getSkill(slot);
            if (skill != null && !AscensionOriginSourceHelper.hasSkill(source, skill)) {
                removeSkill(skill);
            }
        }
    }

    //──Casting────────────────────────────────────────────────────────

    public @Nullable Identifier getCastingSkill() {
        SkillCaster caster = SkillCaster.getExisting(player);
        return caster == null ? null : caster.getCastingSkill();
    }

    public @Nullable ActiveCastVisualState getActiveCastVisualState() {
        SkillCaster caster = SkillCaster.getExisting(player);
        return caster == null ? null : caster.getActiveCastVisualState();
    }

    public boolean isWaitingForCastRelease() {
        return waitingForCastRelease;
    }

    public void releaseCastInput() {
        waitingForCastRelease = false;
    }

    public void requireCastInputRelease() {
        waitingForCastRelease = true;
    }

    public void castSelectedSkill() {
        if (waitingForCastRelease) {
            return;
        }

        SkillSlot<?, ?> slot = hotBar.getSlot(hotBar.getSelectedSlot());
        if (slot == null) {
            return;
        }

        //the hot bar can briefly hold a skill that was removed, never cast it
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source != null && !AscensionOriginSourceHelper.hasSkill(source, slot.getSkillId())) {
            return;
        }

        CastingSkillInstance<?, ?, ?> cast = slot.createCast(player.level().registryAccess());
        getCaster().startCast(cast);
    }

    public void tick() {
        SkillCaster caster = SkillCaster.getExisting(player);
        //a held input that ended a long cast should not immediately start a new one
        if (caster != null && caster.tick()) {
            requireCastInputRelease();
        }
        resolve();
    }

    public void endCast(CastStatus.Reason reason) {
        SkillCaster caster = SkillCaster.getExisting(player);
        if (caster != null) {
            caster.endCast(reason);
        }
    }

    //──Sync────────────────────────────────────────────────────────

    public static class Provider implements IAttachmentSerializer<SkillCastHandler> {
        @Override
        public SkillCastHandler read(IAttachmentHolder holder, ValueInput input) {
            if (!(holder instanceof Player entity)) {
                return null;
            }
            SkillCastHandler handler = new SkillCastHandler(entity);
            handler.hotBar.load(input);
            return handler;
        }

        @Override
        public boolean write(SkillCastHandler attachment, ValueOutput output) {
            attachment.hotBar.write(output);
            return true;
        }
    }

    public static class SyncHandler implements AttachmentSyncHandler<SkillCastHandler> {
        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return holder == to;
        }

        @Override
        public void write(
                RegistryFriendlyByteBuf buf,
                SkillCastHandler attachment,
                boolean initialSync
        ) {
            attachment.hotBar.encode(buf);
        }

        @Override
        public @Nullable SkillCastHandler read(
                IAttachmentHolder holder,
                RegistryFriendlyByteBuf buf,
                @Nullable SkillCastHandler previousValue
        ) {
            if (!(holder instanceof Player player)) {
                return previousValue;
            }

            SkillCastHandler handler = previousValue == null
                    ? new SkillCastHandler(player)
                    : previousValue;
            handler.hotBar.decode(buf);
            return handler;
        }
    }
}
