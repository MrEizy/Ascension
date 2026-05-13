package net.thejadeproject.ascension.refactor_packages.skill_casting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.casting.SyncSlot;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.ISkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;

import java.util.Objects;

public class SkillHotBar {

    public final int MAX_SLOTS;
    private final HotBarSkillSlot[] skillSlots;

    private int activeSlot = 0;

    public SkillHotBar(int maxSlots) {
        MAX_SLOTS = maxSlots;
        skillSlots = new HotBarSkillSlot[maxSlots];

        for (int i = 0; i < MAX_SLOTS; i++) {
            skillSlots[i] = new HotBarSkillSlot();
        }
    }

    public void setActiveSlot(IEntityData entityData, int slot) {
        if (slot < 0 || slot >= skillSlots.length) return;

        if (activeSlot != slot) {
            HotBarSkillSlot oldSlot = skillSlots[activeSlot];
            if (oldSlot.getSkillKey() != null && oldSlot.getSkill() instanceof ICastableSkill castableSkill) {
                castableSkill.unselected(entityData);
            }

            this.activeSlot = slot;

            HotBarSkillSlot newSlot = skillSlots[activeSlot];
            if (newSlot.getSkillKey() != null && newSlot.getSkill() instanceof ICastableSkill newCastableSkill) {
                newCastableSkill.selected(entityData);
            }
        }
    }

    public ISkill getActiveSkill() {
        return skillSlots[activeSlot].getSkill();
    }

    public IPreCastData getPreCastData(int slot) {
        if (slot < 0 || slot >= skillSlots.length) return null;
        return skillSlots[slot].getPreCastData();
    }

    public ResourceLocation getSkillKey(int slot) {
        if (slot < 0 || slot >= skillSlots.length) return null;
        return skillSlots[slot].getSkillKey();
    }

    public ISkill getSkill(int slot) {
        if (slot < 0 || slot >= skillSlots.length) return null;
        return skillSlots[slot].getSkill();
    }

    public int getActiveSlot() {
        return activeSlot;
    }

    public int getSlot(ResourceLocation skill) {
        for (int i = 0; i < skillSlots.length; i++) {
            if (Objects.equals(skill, skillSlots[i].getSkillKey())) {
                return i;
            }
        }

        return -1;
    }

    public ResourceLocation getActiveSkillKey() {
        return skillSlots[activeSlot].getSkillKey();
    }

    public void slotSkill(IEntityData entityData, ResourceLocation skill, int slot) {
        if (skill == null) return;
        if (slot < 0 || slot >= skillSlots.length) return;

        for (int i = 0; i < MAX_SLOTS; i++) {
            if (Objects.equals(skill, skillSlots[i].getSkillKey())) {
                skillSlots[i].unSlotSKill(entityData);
            }
        }

        if (Objects.equals(skill, skillSlots[slot].getSkillKey())) return;

        skillSlots[slot].setSkill(skill, entityData);
    }

    public void slotSkill(IEntityData entityData, ResourceLocation skill, int slot, IPreCastData preCastData) {
        if (skill == null) return;
        if (slot < 0 || slot >= skillSlots.length) return;

        for (int i = 0; i < MAX_SLOTS; i++) {
            if (Objects.equals(skill, skillSlots[i].getSkillKey())) {
                skillSlots[i].unSlotSKill(entityData);
            }
        }

        if (Objects.equals(skill, skillSlots[slot].getSkillKey())) return;

        skillSlots[slot].setSkill(skill, preCastData, entityData);
    }

    public void unSlotSkill(IEntityData entityData, int slot) {
        if (slot < 0 || slot >= skillSlots.length) return;
        skillSlots[slot].unSlotSKill(entityData);
    }

    public void unSlotSkill(IEntityData entityData, ResourceLocation skill) {
        if (skill == null) return;

        for (HotBarSkillSlot skillSlot : skillSlots) {
            if (Objects.equals(skillSlot.getSkillKey(), skill)) {
                skillSlot.unSlotSKill(entityData);
            }
        }
    }

    public void syncSlots(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        for (int i = 0; i < skillSlots.length; i++) {
            if (skillSlots[i].isDirty()) {
                PacketDistributor.sendToPlayer(
                        serverPlayer,
                        new SyncSlot(i, skillSlots[i].getSkillKey(), skillSlots[i].getPreCastData())
                );
                skillSlots[i].resolve();
            }
        }
    }

    /*
        used when it is detected a change was made to the skill list
     */
    public void refreshSkillSlots(IEntityData entityData) {
        // TODO
    }

    public void write(CompoundTag tag) {
        ListTag hotBarTag = new ListTag();

        for (int i = 0; i < skillSlots.length; i++) {
            if (skillSlots[i].isEmpty()) continue;
            if (skillSlots[i].getSkillKey() == null) continue;

            CompoundTag slotTag = new CompoundTag();
            slotTag.putString("identifier", skillSlots[i].getSkillKey().toString());

            if (skillSlots[i].getPreCastData() != null) {
                slotTag.put("pre_cast_data", skillSlots[i].getPreCastData().write());
            }

            slotTag.putInt("slot", i);
            hotBarTag.add(slotTag);
        }

        tag.put("hot_bar", hotBarTag);
    }

    public void read(CompoundTag tag) {
        ListTag hotBarTag = tag.getList("hot_bar", Tag.TAG_COMPOUND);

        for (int i = 0; i < hotBarTag.size(); i++) {
            CompoundTag slotTag = hotBarTag.getCompound(i);

            if (!slotTag.contains("identifier")) continue;

            ResourceLocation skillIdentifier = ResourceLocation.parse(slotTag.getString("identifier"));
            int slot = slotTag.getInt("slot");

            if (slot < 0 || slot >= skillSlots.length) continue;

            IPreCastData preCastData = null;

            ISkill skill = AscensionRegistries.Skills.SKILL_REGISTRY.get(skillIdentifier);
            if (slotTag.contains("pre_cast_data") && skill instanceof ICastableSkill castableSkill) {
                preCastData = castableSkill.preCastDataFromCompound(slotTag.getCompound("pre_cast_data"));
            }

            skillSlots[slot] = new HotBarSkillSlot(skillIdentifier, preCastData);
        }
    }
}