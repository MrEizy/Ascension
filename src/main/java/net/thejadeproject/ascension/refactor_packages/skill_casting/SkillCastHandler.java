package net.thejadeproject.ascension.refactor_packages.skill_casting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndReason;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastingInstance;
import net.thejadeproject.ascension.refactor_packages.skill_casting.persistent_casting.PersistentCastingInstance;
import net.thejadeproject.ascension.refactor_packages.skills.castable.CastType;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPersistentSkillInstance;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

//Do not worry about cast logic yet
public class SkillCastHandler {


    private final CastingInstance castingInstance = new CastingInstance();

    private final ArrayList<PersistentCastingInstance> persistentCastingInstances = new ArrayList<>();

    private final SkillCooldownHandler cooldownHandler = new SkillCooldownHandler();


    private final SkillHotBar hotBar = new SkillHotBar(5);

    public void addPersistentCastingInstance(ResourceLocation skill, IPersistentSkillInstance skillInstance){
        persistentCastingInstances.add(new PersistentCastingInstance(skillInstance,skill));
    }
    public void removePersistentCastingInstances(int instanceIndex){
        persistentCastingInstances.remove(instanceIndex);
    }

    public void tick(Entity entity){
        IEntityData entityData = entity.getData(ModAttachments.ENTITY_DATA);
        getCooldownHandler().tick(entityData);
        CastEndData castEndData =  castingInstance.castTick(entity);
        if(castEndData != null){
            int cooldownTime = ((ICastableSkill) castEndData.getSkill()).getCooldown(castEndData);
            getCooldownHandler().addCooldown(castEndData.skillId(),cooldownTime);
        }
        ArrayList<PersistentCastingInstance> toRemove = new ArrayList<>();
        for(PersistentCastingInstance instance : getPersistentInstances()){
            if(!instance.tick(entity)) toRemove.add(instance);
        }
        for(PersistentCastingInstance instance: toRemove){
            persistentCastingInstances.remove(instance);
        }

    }

    public SkillCooldownHandler getCooldownHandler(){
        return cooldownHandler;
    }
    public CastingInstance getCastingInstance(){
        return castingInstance;
    }
    public List<PersistentCastingInstance> getPersistentInstances(){
        return persistentCastingInstances;
    }

    public void setSelectedSkill(IEntityData entityData,int selectedSkill){
        hotBar.setActiveSlot(entityData,selectedSkill);
    }

    public boolean isCasting(){
        return castingInstance.isCasting();
    }

    //server side
    //TODO get it to send message to client on fail
    public void tryCast(Entity entity) {
        if (hotBar.getSkillKey(hotBar.getActiveSlot()) == null) return;
        if (!(hotBar.getActiveSkill() instanceof ICastableSkill castableSkill)) return;

        ResourceLocation activeSkillKey = hotBar.getActiveSkillKey();

        if (cooldownHandler.isOnCooldown(activeSkillKey)) {
            return;
        }

        IPreCastData preCastData = hotBar.getPreCastData(hotBar.getActiveSlot());

        CastResult result = castableSkill.canCast(entity, preCastData);

        if (!result.isSuccess()) {
            return;
        }

        if (castingInstance.isCasting()) {
            CastEndData endData = castingInstance.endCast(entity, CastEndReason.CANCELLED);
            if (endData != null) {
                int cooldownTime = ((ICastableSkill) endData.getSkill()).getCooldown(endData);
                cooldownHandler.addCooldown(endData.skillId(), cooldownTime);
            }
        }

        castableSkill.initialCast(entity, preCastData);

        if (castableSkill.getCastType() == CastType.INSTANT) {
            CastEndData endData = new CastEndData(
                    activeSkillKey,
                    CastEndReason.ENDED,
                    null,
                    0
            );

            int cooldownTime = castableSkill.getCooldown(endData);
            cooldownHandler.addCooldown(activeSkillKey, cooldownTime);
            return;
        }

        if (castableSkill.getCastType() == CastType.LONG) {
            castingInstance.startCast(entity, activeSkillKey);
        }
    }


    public int getMaxSlots(){
        return hotBar.MAX_SLOTS;
    }
    public SkillHotBar getHotBar(){
        return hotBar;
    }

    public CompoundTag write(){
        CompoundTag tag = new CompoundTag();
        getCooldownHandler().write(tag);
        getHotBar().write(tag);
        return tag;
    }
    public void read(CompoundTag tag){
        try {
            getCooldownHandler().read(tag);
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error loading cooldown handler",e);
        }
        try {
            getHotBar().read(tag);
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error loading hotbar",e);
        }


    }
    public void sync(Player player){
        getHotBar().syncSlots(player);
    }
}
