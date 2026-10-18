package net.zic.ascension.skill_casting.hotbar;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.Config;
import net.zic.ascension.api.ascension.core.skill.castable.SkillSlot;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

//the slot count comes from the server config, clients take the size from the sync
public class SkillHotBar {
    private final LivingEntity owner;
    //null entries are empty slots
    private @Nullable SkillSlot<?, ?>[] slots;

    private int selectedSlot;
    private boolean dirty;

    public SkillHotBar(LivingEntity owner) {
        this.owner = owner;
        slots = new SkillSlot<?, ?>[configuredSlots()];
    }

    public @Nullable SkillSlot<?, ?> getSlot(int slot) {
        return isValidSlot(slot) ? slots[slot] : null;
    }

    public @Nullable Identifier getSkill(int slot) {
        SkillSlot<?, ?> skillSlot = getSlot(slot);
        return skillSlot == null ? null : skillSlot.getSkillId();
    }

    //the first slot holding this skill, used to give a synced cast its equip data
    public @Nullable SkillSlot<?, ?> findSlot(Identifier skill) {
        for (SkillSlot<?, ?> slot : slots) {
            if (slot != null && slot.getSkillId().equals(skill)) return slot;
        }
        return null;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    //slots a skill, null clears the slot. a skill that is not castable is ignored
    public void slotSkill(@Nullable Identifier skill, int slot) {
        if (!isValidSlot(slot)) {
            return;
        }
        SkillSlot<?, ?> replacement = skill == null ? null : SkillSlot.create(owner, skill, access(), this::markDirty);
        if (skill != null && replacement == null) {
            return;
        }

        boolean selected = slot == selectedSlot;
        SkillSlot<?, ?> previous = slots[slot];
        if (previous != null) {
            if (selected) previous.unselect();
            previous.unequip();
        }

        slots[slot] = replacement;
        if (replacement != null) {
            replacement.equip();
            if (selected) replacement.select();
        }
        markDirty();
    }

    public void select(int slot) {
        if (!isValidSlot(slot) || selectedSlot == slot) {
            return;
        }

        if (slots[selectedSlot] != null) slots[selectedSlot].unselect();
        selectedSlot = slot;
        if (slots[selectedSlot] != null) slots[selectedSlot].select();
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
        return slots.length;
    }

    //the config may not be loaded yet when the hot bar is first created, the sync or load corrects the size
    private static int configuredSlots() {
        try {
            return Config.COMMON.SKILL_HOT_BAR_SLOTS.get();
        } catch (IllegalStateException e) {
            return Config.COMMON.SKILL_HOT_BAR_SLOTS.getDefault();
        }
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < slots.length;
    }

    private RegistryAccess access() {
        return owner.level().registryAccess();
    }

    //──Serialization────────────────────────────────────────────────────────

    public void write(ValueOutput output) {
        output.putInt("selected", selectedSlot);
        NbtHelpers.writeArray(output, "slots", slots, (holder, id, value) -> {
            if (value != null) value.write(holder, access());
        });
    }

    public void load(ValueInput input) {
        //readList does not allow null entries, so empty slots are wrapped
        List<Optional<SkillSlot<?, ?>>> loadedSlots = NbtHelpers.readList(input, "slots", (holder, id) ->
                Optional.ofNullable(SkillSlot.read(owner, holder, access(), this::markDirty))
        );
        replaceSlots(loadedSlots.stream().<SkillSlot<?, ?>>map(slot -> slot.orElse(null)).toList(), configuredSlots(), input.getIntOr("selected", 0));
        markDirty();
    }

    public void encode(ByteBuf buf) {
        ByteBufHelpers.encodeArray(slots, buf, (value, target) -> {
            target.writeBoolean(value != null);
            if (value != null) value.encode(target, access());
        });
        buf.writeInt(selectedSlot);
    }

    public void decode(ByteBuf buf) {
        List<SkillSlot<?, ?>> decodedSlots = ByteBufHelpers.decodeArray(buf, source ->
                source.readBoolean() ? SkillSlot.decode(owner, source, access(), this::markDirty) : null
        );
        replaceSlots(decodedSlots, decodedSlots.size(), buf.readInt());
        resolveDirty();
    }

    /**
     * replaces the slots with loaded or synced ones, resizing the hot bar. only slots whose skill changed are
     * unequipped/equipped, slots that kept their skill just take the new equip data, so syncs do not re-run hooks
     */
    private void replaceSlots(List<SkillSlot<?, ?>> replacements, int size, int replacementSelectedSlot) {
        int newSize = Math.max(1, size);
        SkillSlot<?, ?>[] next = new SkillSlot<?, ?>[newSize];
        for (int index = 0; index < newSize && index < replacements.size(); index++) {
            next[index] = replacements.get(index);
        }
        int nextSelectedSlot = replacementSelectedSlot >= 0 && replacementSelectedSlot < newSize ? replacementSelectedSlot : 0;

        boolean selectionChanged = nextSelectedSlot != selectedSlot
                || !Objects.equals(getSkill(selectedSlot), next[nextSelectedSlot] == null ? null : next[nextSelectedSlot].getSkillId());
        if (selectionChanged && getSlot(selectedSlot) != null) {
            slots[selectedSlot].unselect();
        }

        for (int index = 0; index < Math.max(slots.length, newSize); index++) {
            SkillSlot<?, ?> current = index < slots.length ? slots[index] : null;
            SkillSlot<?, ?> replacement = index < newSize ? next[index] : null;
            if (current != null && replacement != null && current.getSkillId().equals(replacement.getSkillId())) {
                current.copyEquipDataFrom(replacement);
                next[index] = current;
                continue;
            }
            if (current != null) current.unequip();
            if (replacement != null) replacement.equip();
        }

        slots = next;
        selectedSlot = nextSelectedSlot;
        if (selectionChanged && slots[selectedSlot] != null) {
            slots[selectedSlot].select();
        }
    }
}
