package net.zic.ascension.skill_casting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.SkillSlot;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.event.skill.SkillCastEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.control.StaggerService;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * the skill an entity is casting, any living entity can cast.
 * <p>
 * The server is authoritative. The cast is synced to every player tracking the entity (and the entity itself),
 * but only when it starts, ends, changes phase or its cast data changes, along with the server's ticks into the phase.
 * Between syncs clients advance the phase ticks themselves. The casting player's client also predicts the cast,
 * other clients only advance the ticks and never run skill logic.
 * <p>
 * Changes only mark the caster dirty, it syncs at most once per tick from {@link #tick()}.
 */
public class SkillCaster {
    //the owning client keeps its predicted ticks if they are within this many ticks of the server
    private static final int PREDICTION_TOLERANCE = 3;
    //client only, entity ids that have a synced cast, read by client rendering
    private static final Set<Integer> CLIENT_CASTERS = new HashSet<>();

    private final LivingEntity entity;
    private @Nullable CastingSkillInstance<?, ?, ?> casting;
    //a cast started or ended since the last sync
    private boolean castChanged;

    public SkillCaster(LivingEntity entity) {
        this.entity = entity;
    }

    //the caster for an entity, creating it
    public static SkillCaster of(LivingEntity entity) {
        return entity.getData(AscensionAttachments.ASCENSION_SKILL_CASTER);
    }

    //the caster for an entity if it has ever cast, so lookups do not attach a caster to every entity
    public static @Nullable SkillCaster getExisting(LivingEntity entity) {
        return entity.getExistingDataOrNull(AscensionAttachments.ASCENSION_SKILL_CASTER);
    }

    //client only, ids of entities with a synced cast. stale ids are expected, check the entity
    public static Set<Integer> clientCasters() {
        return CLIENT_CASTERS;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public @Nullable CastingSkillInstance<?, ?, ?> getCasting() {
        return casting;
    }

    public @Nullable Identifier getCastingSkill() {
        return casting == null ? null : casting.getSkillId();
    }

    public @Nullable ActiveCastVisualState getActiveCastVisualState() {
        return casting == null ? null : casting.visualState();
    }

    private RegistryAccess access() {
        return entity.level().registryAccess();
    }

    //the casting player's own client, the only client that runs skill logic
    private boolean isPredicting() {
        return entity.level().isClientSide() && entity instanceof Player player && player.isLocalPlayer();
    }

    //──Lifecycle────────────────────────────────────────────────────────

    /**
     * attempts to start a cast, ending the current one
     * @return true if the cast started
     */
    public boolean startCast(CastingSkillInstance<?, ?, ?> next) {
        if (casting != null && casting.getSkillId().equals(next.getSkillId())) {
            return false;
        }
        //checked before ending the current cast, so a failed attempt does not cancel it
        if (StaggerService.isGuardBroken(entity)) {
            sendMessage(Component.literal("Guard broken"));
            return false;
        }

        endCast(CastStatus.Reason.CANCELLED);

        CastResult result = next.tryCast();
        if (!result.isSuccess()) {
            if (result.message != null) {
                sendMessage(result.message);
            }
            return false;
        }

        boolean server = !entity.level().isClientSide();
        if (server && NeoForge.EVENT_BUS.post(new SkillCastEvent.Start.Pre(next)).isCanceled()) {
            return false;
        }

        casting = next;
        castChanged = true;
        next.start();
        if (server) {
            NeoForge.EVENT_BUS.post(new SkillCastEvent.Start.Post(next));
        }
        //instant skills end in startCast
        if (!next.isCasting()) {
            finishCast();
        }
        return true;
    }

    public void endCast(CastStatus.Reason reason) {
        if (casting == null) {
            return;
        }
        casting.end(reason);
        finishCast();
    }

    //ends the cast without letting it resolve, used when the caster leaves (logout, death)
    public void invalidateCast() {
        endCast(CastStatus.Reason.INVALIDATED);
    }

    public void interruptCast() {
        endCast(CastStatus.Reason.INTERRUPTED);
    }

    public void recordDamage(double damage) {
        if (casting == null) {
            return;
        }
        casting.onDamaged(damage);
        if (!casting.isCasting()) {
            finishCast();
        }
    }

    private void finishCast() {
        CastingSkillInstance<?, ?, ?> ended = casting;
        if (ended == null) {
            return;
        }
        casting = null;
        castChanged = true;
        ended.finish();
        if (!entity.level().isClientSide()) {
            NeoForge.EVENT_BUS.post(new SkillCastEvent.End(ended));
        }
    }

    /**
     * ticks the cast
     * @return true if a cast ended this tick while its input was still held
     */
    public boolean tick() {
        //other clients only advance the ticks, the server decides when the phase or cast changes
        if (entity.level().isClientSide() && !isPredicting()) {
            if (casting != null) casting.advance();
            return false;
        }

        boolean endedWithInputHeld = false;
        if (casting != null) {
            casting.tick();
            if (!casting.isCasting()) {
                endedWithInputHeld = casting.isInputHeld();
                finishCast();
            }
        }
        resolve();
        return endedWithInputHeld;
    }

    private void sendMessage(Component message) {
        //both sides attempt the cast, only the server shows the result so it is not shown twice
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            player.sendOverlayMessage(message);
        }
    }

    //──Sync────────────────────────────────────────────────────────

    public void resolve() {
        if (entity.level().isClientSide()) {
            return;
        }
        if (castChanged || casting != null && casting.isDirty()) {
            entity.syncData(AscensionAttachments.ASCENSION_SKILL_CASTER);
            //cleared here rather than in write, write is skipped when nobody is tracking the entity
            resolveDirty();
        }
    }

    private void encodeCast(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(casting != null);
        if (casting != null) {
            casting.encode(buf, access());
        }
    }

    private void resolveDirty() {
        if (casting != null) {
            casting.resolveDirty();
        }
        castChanged = false;
    }

    //applies the server's cast, without running tryCast/startCast on this side
    private void decodeCast(RegistryFriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            endCast(CastStatus.Reason.INVALIDATED);
            castChanged = false;
            return;
        }

        Identifier skillId = CastingSkillInstance.decodeSkillId(buf);
        if (casting == null || !casting.getSkillId().equals(skillId)) {
            endCast(CastStatus.Reason.INVALIDATED);
            casting = createSyncedCast(skillId);
        }
        if (casting == null) {
            CastingSkillInstance.skipState(buf);
        } else {
            casting.decodeState(buf, access(), isPredicting() ? PREDICTION_TOLERANCE : 0);
        }
        castChanged = false;
    }

    //a synced cast takes the equip data from the caster's slot if they have one
    private @Nullable CastingSkillInstance<?, ?, ?> createSyncedCast(Identifier skillId) {
        if (entity instanceof Player player) {
            SkillCastHandler handler = player.getExistingDataOrNull(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
            SkillSlot<?, ?> slot = handler == null ? null : handler.findSlot(skillId);
            if (slot != null) {
                return slot.createCast(access());
            }
        }
        return CastingSkillInstance.create(entity, skillId, access());
    }

    public static class SyncHandler implements AttachmentSyncHandler<SkillCaster> {
        @Override
        public void write(RegistryFriendlyByteBuf buf, SkillCaster attachment, boolean initialSync) {
            attachment.encodeCast(buf);
        }

        @Override
        public @Nullable SkillCaster read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable SkillCaster previousValue) {
            if (!(holder instanceof LivingEntity entity)) {
                return previousValue;
            }
            SkillCaster caster = previousValue == null ? new SkillCaster(entity) : previousValue;
            caster.decodeCast(buf);
            if (caster.casting != null) {
                CLIENT_CASTERS.add(entity.getId());
            } else {
                CLIENT_CASTERS.remove(entity.getId());
            }
            return caster;
        }
    }
}
