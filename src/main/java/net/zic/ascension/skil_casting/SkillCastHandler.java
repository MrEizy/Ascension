package net.zic.ascension.skil_casting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.skill.castable.PreCastData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.SourceChangesSnapshot;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import net.zic.ascension.skil_casting.hotbar.SkillHotBar;
import net.zic.ascension.skil_casting.hotbar.SkillHotBarSlot;
import net.zic.zenithlib.common.ZenithAttachments;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SkillCastHandler {
    private final Player player;
    private final CastingInstance instance = new CastingInstance();
    private final SkillHotBar hotBar = new SkillHotBar();


    public SkillCastHandler(Player player) {
        this.player = player;
    }

    public Player getPlayer(){
        return player;
    }

    public void resolve(){
        if(hotBar.isDirty() || instance.isDirty()){
            player.syncData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        }
    }

    public void slotSkill(Identifier skill,int slot){
        hotBar.slotSkill(player,skill,slot);
    }
    public void select(int slot){
        hotBar.select(player,slot);
    }


    //generic for items with skills and stuff
    public void castSkill(Identifier skill, PreCastData castData){
        //TODO
    }

    public void castSelectedSkill(){
        //TODO
    }

    public static class Provider implements IAttachmentSerializer<SkillCastHandler>{

        @Override
        public SkillCastHandler read(IAttachmentHolder holder, ValueInput input) {
            if(!(holder instanceof Player entity)) return null;
            SkillCastHandler handle = new SkillCastHandler(entity);
            handle.hotBar.load(input,entity);
            return handle;
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
        public void write(RegistryFriendlyByteBuf buf, SkillCastHandler attachment, boolean initialSync) {
            buf.writeBoolean(attachment.hotBar.isDirty());
            if(attachment.hotBar.isDirty()){

                attachment.hotBar.encode(buf);
                attachment.hotBar.resolveDirty();
            }
            buf.writeBoolean(attachment.instance.isDirty());
            if(attachment.instance.isDirty()){
                attachment.instance.encode(buf);
                attachment.instance.resolveDirty();;
            }
        }

        @Override
        public @Nullable SkillCastHandler read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable SkillCastHandler previousValue) {
            if(previousValue == null) previousValue = new SkillCastHandler((Player) holder);
            if(buf.readBoolean()){
                previousValue.hotBar.decode(buf,previousValue.getPlayer());
            }
            if(buf.readBoolean()){
                previousValue.instance.decode(buf,previousValue.getPlayer());
            }
            return previousValue;
        }
    }


}
