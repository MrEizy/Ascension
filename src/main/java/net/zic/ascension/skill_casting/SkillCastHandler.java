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
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.castable.CastableSkill;
import net.zic.ascension.api.core.skill.castable.PreCastData;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.hotbar.SkillHotBar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// TODO listen to skill-cast end and resolve immediately.
public class SkillCastHandler {
    private final Player player;
    private final CastingInstance instance = new CastingInstance();
    private final SkillHotBar hotBar = new SkillHotBar();

    public SkillCastHandler(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void resolve() {
        if (player.level().isClientSide()) {
            return;
        }
        if (hotBar.isDirty() || instance.isDirty()) {
            player.syncData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        }
    }

    public int getMaxSlots() {
        return hotBar.getMaxSlots();
    }

    public int getSelectedSlot() {
        return hotBar.getSelectedSlot();
    }

    public Identifier getSkill(int slot) {
        return hotBar.getSkill(slot);
    }

    public PreCastData getPreCastData(int slot) {
        return hotBar.getPreCastData(slot);
    }

    public void markHotBarDirty() {
        hotBar.markDirty();
        resolve();
    }

    public void slotSkill(Identifier skill, int slot) {
        if (slot < 0 || slot >= getMaxSlots()) {
            return;
        }
        hotBar.slotSkill(player, skill, slot);
    }

    public void select(int slot) {
        if (slot < 0 || slot >= getMaxSlots()) {
            return;
        }
        hotBar.select(player, slot);
    }

    public void castSkill(Identifier skill, PreCastData castData) {
        instance.startCast(player, skill, castData);
        resolve();
    }

    public void castSelectedSkill() {
        Identifier skill = hotBar.getSkill(hotBar.getSelectedSlot());
        if (skill == null) {
            return;
        }
        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                player.registryAccess()
        ) instanceof CastableSkill)) {
            return;
        }

        castSkill(skill, hotBar.getPreCastData(hotBar.getSelectedSlot()));
    }

    public void tick() {
        instance.continueCasting(player);
        resolve();
    }

    public static class Provider implements IAttachmentSerializer<SkillCastHandler> {
        @Override
        public SkillCastHandler read(IAttachmentHolder holder, ValueInput input) {
            if (!(holder instanceof Player entity)) {
                return null;
            }
            SkillCastHandler handler = new SkillCastHandler(entity);
            handler.hotBar.load(input, entity);
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
            boolean syncHotBar = initialSync || attachment.hotBar.isDirty();
            buf.writeBoolean(syncHotBar);
            if (syncHotBar) {
                attachment.hotBar.encode(buf);
                attachment.hotBar.resolveDirty();
            }

            boolean syncCasting = initialSync || attachment.instance.isDirty();
            buf.writeBoolean(syncCasting);
            if (syncCasting) {
                attachment.instance.encode(buf);
                attachment.instance.resolveDirty();
            }
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
            if (buf.readBoolean()) {
                handler.hotBar.decode(buf, player);
            }
            if (buf.readBoolean()) {
                handler.instance.decode(buf, player);
            }
            return handler;
        }
    }
}
