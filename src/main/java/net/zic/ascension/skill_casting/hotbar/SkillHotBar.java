package net.zic.ascension.skill_casting.hotbar;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.PreCastData;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.List;

// TODO update to include a config option for skill slots
public class SkillHotBar {
    private final SkillHotBarSlot[] slots;

    private int selectedSlot;
    private boolean dirty;

    public SkillHotBar() {
        slots = new SkillHotBarSlot[getMaxSlots()];
        for (int index = 0; index < slots.length; index++) {
            slots[index] = new SkillHotBarSlot();
        }
    }

    public Identifier getSkill(int slot) {
        return isValidSlot(slot) ? slots[slot].getSkill() : null;
    }

    public PreCastData getPreCastData(int slot) {
        return isValidSlot(slot) ? slots[slot].getPreCastData() : null;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void slotSkill(LivingEntity entity, Identifier skill, int slot) {
        if (!isValidSlot(slot)) {
            return;
        }

        boolean selected = slot == selectedSlot;
        if (selected) {
            notifySelection(entity, slot, false);
        }

        slots[slot].setSkill(entity, skill);

        if (selected) {
            notifySelection(entity, slot, true);
        }
        markDirty();
    }

    public void select(LivingEntity entity, int slot) {
        if (!isValidSlot(slot) || selectedSlot == slot) {
            return;
        }

        notifySelection(entity, selectedSlot, false);
        selectedSlot = slot;
        notifySelection(entity, selectedSlot, true);
        markDirty();
    }

    public void validateSlots(LivingEntity entity) {
        if (!isValidSlot(selectedSlot)) {
            selectedSlot = 0;
        }
        markDirty();
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

    public int getMaxSlots() {
        return 6;
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < slots.length;
    }

    private void notifySelection(LivingEntity entity, int slot, boolean selected) {
        Identifier skillId = getSkill(slot);
        if (skillId == null || !(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                entity.level().registryAccess()
        ) instanceof CastableSkill castableSkill)) {
            return;
        }

        if (selected) {
            castableSkill.selected(entity, getPreCastData(slot));
        } else {
            castableSkill.unselected(entity, getPreCastData(slot));
        }
    }

    public void write(ValueOutput output) {
        output.putInt("selected", selectedSlot);
        NbtHelpers.writeArray(output, "slots", slots, (holder, id, value) -> {
            if (value.getSkill() == null) {
                return;
            }
            NbtHelpers.writeIdentifier(holder, "skill", value.getSkill());
            if (value.getPreCastData() != null) {
                value.getPreCastData().write(holder.child("pre_cast_data"));
            }
        });
    }

    public void load(ValueInput input, LivingEntity entity) {
        List<SkillHotBarSlot> loadedSlots = NbtHelpers.readList(input, "slots", (holder, id) -> {
            SkillHotBarSlot slot = new SkillHotBarSlot();
            if (holder.getString("skill").isEmpty()) {
                return slot;
            }

            Identifier skill = NbtHelpers.readIdentifier(holder, "skill");
            if (!(CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skill,
                    entity.level().registryAccess()
            ) instanceof CastableSkill castableSkill)) {
                return slot;
            }

            PreCastData data = castableSkill.loadPreCastData(
                    holder.childOrEmpty("pre_cast_data")
            );
            slot.setSkill(entity, skill, data);
            return slot;
        });

        replaceSlots(entity, loadedSlots, input.getIntOr("selected", 0));
        markDirty();
    }

    public void encode(ByteBuf buf) {
        ByteBufHelpers.encodeArray(slots, buf, (value, target) -> {
            target.writeBoolean(value.getSkill() != null);
            if (value.getSkill() == null) {
                return;
            }

            ByteBufHelpers.encodeIdentifier(value.getSkill(), target);
            target.writeBoolean(value.getPreCastData() != null);
            if (value.getPreCastData() != null) {
                value.getPreCastData().encode(target);
            }
        });
        buf.writeInt(selectedSlot);
    }

    public void decode(RegistryFriendlyByteBuf buf, LivingEntity entity) {
        List<SkillHotBarSlot> decodedSlots = ByteBufHelpers.decodeArray(buf, source -> {
            SkillHotBarSlot slot = new SkillHotBarSlot();
            if (!source.readBoolean()) {
                return slot;
            }

            Identifier skill = ByteBufHelpers.decodeIdentifier(source);
            if (!(CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skill,
                    buf.registryAccess()
            ) instanceof CastableSkill castableSkill)) {
                return slot;
            }

            PreCastData preCastData = source.readBoolean()
                    ? castableSkill.loadPreCastData(source)
                    : null;
            slot.setSkill(entity, skill, preCastData);
            return slot;
        });
        int decodedSelectedSlot = buf.readInt();

        replaceSlots(entity, decodedSlots, decodedSelectedSlot);
        resolveDirty();
    }

    private void replaceSlots(
            LivingEntity entity,
            List<SkillHotBarSlot> replacements,
            int replacementSelectedSlot
    ) {
        notifySelection(entity, selectedSlot, false);
        for (SkillHotBarSlot slot : slots) {
            slot.unslotSkill(entity);
        }

        for (int index = 0; index < slots.length; index++) {
            slots[index] = index < replacements.size()
                    ? replacements.get(index)
                    : new SkillHotBarSlot();
        }

        selectedSlot = isValidSlot(replacementSelectedSlot) ? replacementSelectedSlot : 0;
        notifySelection(entity, selectedSlot, true);
    }
}
