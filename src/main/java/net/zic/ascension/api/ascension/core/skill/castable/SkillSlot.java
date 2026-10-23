package net.zic.ascension.api.ascension.core.skill.castable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

/**
 * A castable skill in a hot bar slot.
 * Wraps the skill and its equip data, and defers calls to the skill so it always receives its own typed data.
 */
public final class SkillSlot<D extends SkillData, E extends EquipSkillData> {
    private final LivingEntity owner;
    private final Identifier skillId;
    private final CastableSkill<D, ?, E> skill;
    private E equipData;
    private final Runnable markHotBarDirty;

    private SkillSlot(LivingEntity owner, Identifier skillId, CastableSkill<D, ?, E> skill, E equipData, Runnable markHotBarDirty) {
        this.owner = owner;
        this.skillId = skillId;
        this.skill = skill;
        this.equipData = equipData;
        this.markHotBarDirty = markHotBarDirty;
    }

    /**
     * creates a slot with fresh equip data, not yet equipped
     * @param markHotBarDirty marks the owning hot bar dirty, called when the equip data changes
     * @return the slot, or null if the skill does not exist or is not castable
     */
    public static @Nullable SkillSlot<?, ?> create(LivingEntity owner, Identifier skillId, RegistryAccess access, Runnable markHotBarDirty) {
        CastableSkill<?, ?, ?> skill = CastableSkillLookup.castable(skillId, access);
        return skill == null ? null : createTyped(owner, skillId, skill, access, markHotBarDirty);
    }

    private static <D extends SkillData, E extends EquipSkillData> SkillSlot<D, E> createTyped(
            LivingEntity owner, Identifier skillId, CastableSkill<D, ?, E> skill, RegistryAccess access, Runnable markHotBarDirty
    ) {
        return new SkillSlot<>(owner, skillId, skill, skill.newEquipData(access), markHotBarDirty);
    }

    //──Getters────────────────────────────────────────────────────────
    public LivingEntity getOwner() {return owner;}
    public Identifier getSkillId() {return skillId;}
    public CastableSkill<D, ?, E> getSkill() {return skill;}
    public E getEquipData() {return equipData;}

    //the owner's skill data for this skill, null if the owner does not have the skill
    public @Nullable D getSkillData() {
        return CastableSkillLookup.skillData(owner, skillId, skill);
    }

    //call after changing the skill data, syncs it
    public void markSkillDataDirty() {
        CastableSkillLookup.markSkillDataDirty(owner, skillId);
    }

    //call after changing the equip data, syncs the hot bar
    public void markEquipDataDirty() {
        markHotBarDirty.run();
    }

    //──Dispatch────────────────────────────────────────────────────────

    public void equip() {
        skill.onEquip(this);
    }

    public void unequip() {
        skill.onUnequip(this);
    }

    public void select() {
        skill.onSelected(this);
    }

    public void unselect() {
        skill.onUnselected(this);
    }

    //starts a cast of this slot, the cast receives this slot's equip data
    public CastingSkillInstance<D, ?, E> createCast(RegistryAccess access) {
        return CastingSkillInstance.create(owner, skillId, skill, equipData, access);
    }

    //──Serialization────────────────────────────────────────────────────────

    public void write(ValueOutput output, RegistryAccess access) {
        NbtHelpers.writeIdentifier(output, "skill", skillId);
        skill.equipSerializerHandler().write(equipData, output.child("equip_data"), access);
    }

    //loads a saved slot, not yet equipped. returns null if the skill no longer exists or is not castable
    public static @Nullable SkillSlot<?, ?> read(LivingEntity owner, ValueInput input, RegistryAccess access, Runnable markHotBarDirty) {
        String rawId = input.getStringOr("skill", "");
        if (rawId.isEmpty()) return null;
        Identifier skillId = Identifier.tryParse(rawId);
        if (skillId == null) return null;
        SkillSlot<?, ?> slot = create(owner, skillId, access, markHotBarDirty);
        if (slot != null) slot.readEquipData(input, access);
        return slot;
    }

    private void readEquipData(ValueInput input, RegistryAccess access) {
        E loaded = input.child("equip_data").map(child -> skill.equipSerializerHandler().read(child, access)).orElse(null);
        if (loaded != null) equipData = loaded;
    }

    //the equip data is length prefixed so a skill the client does not know can be skipped
    public void encode(ByteBuf buf, RegistryAccess access) {
        ByteBufHelpers.encodeIdentifier(skillId, buf);
        ByteBuf dataBuf = Unpooled.buffer();
        skill.equipSyncHandler().encode(equipData, dataBuf, access);
        buf.writeInt(dataBuf.readableBytes());
        buf.writeBytes(dataBuf);
    }

    //decodes a slot, not yet equipped. returns null if the skill does not exist on this side
    public static @Nullable SkillSlot<?, ?> decode(LivingEntity owner, ByteBuf buf, RegistryAccess access, Runnable markHotBarDirty) {
        Identifier skillId = ByteBufHelpers.decodeIdentifier(buf);
        ByteBuf dataBuf = buf.readSlice(buf.readInt());
        SkillSlot<?, ?> slot = create(owner, skillId, access, markHotBarDirty);
        if (slot != null) slot.decodeEquipData(dataBuf, access);
        return slot;
    }

    private void decodeEquipData(ByteBuf buf, RegistryAccess access) {
        E decoded = skill.newEquipData(access);
        skill.equipSyncHandler().decode(decoded, buf, access);
        equipData = decoded;
    }

    //takes the equip data of a decoded slot for the same skill, without re-equipping
    public void copyEquipDataFrom(SkillSlot<?, ?> other) {
        if (other.skill == skill) {
            @SuppressWarnings("unchecked") E data = (E) other.equipData;
            equipData = data;
        }
    }
}
