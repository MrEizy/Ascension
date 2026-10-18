package net.zic.ascension.api.ascension.core.skill;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A skill held by an origin source.
 * Wraps a Skill and its data, and defers calls to the skill so it always receives its own typed data.
 */
public final class SkillInstance<D extends SkillData> {
    private final Identifier skillId;
    private final Skill<D> skill;
    private final D data;

    SkillInstance(Identifier skillId, Skill<D> skill, D data) {
        this.skillId = skillId;
        this.skill = skill;
        this.data = data;
    }

    //creates an instance with fresh data
    public static <D extends SkillData> SkillInstance<D> create(Identifier skillId, Skill<D> skill, RegistryAccess access) {
        return new SkillInstance<>(skillId, skill, skill.newData(access));
    }

    //creates a fresh instance by looking up the skill, null if it does not exist
    public static @Nullable SkillInstance<?> create(Identifier skillId, RegistryAccess access) {
        Skill<?> skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, access);
        return skill == null ? null : create(skillId, skill, access);
    }

    //──Getters────────────────────────────────────────────────────────
    public Identifier getSkillId() {return skillId;}
    public Skill<D> getSkill() {return skill;}
    public D getData() {return data;}

    /**
     * narrows this instance to a concrete skill class, e.g. {@code instance.as(PassiveSkill.class)}.
     * the data is always created by the skill, so it always matches the skill's data type
     * @return this instance typed to the skill, or null if the skill is not of that class
     */
    @SuppressWarnings("unchecked")
    public <E extends SkillData, S extends Skill<E>> @Nullable SkillInstance<E> as(Class<S> skillClass) {
        return skillClass.isInstance(skill) ? (SkillInstance<E>) this : null;
    }

    //──Dispatch────────────────────────────────────────────────────────

    public void onAdded(OriginSource source) {
        skill.onAdded(source, this);
    }

    public void onRemoved(OriginSource source) {
        skill.onRemoved(source, this);
    }

    public void applyToEntity(LivingEntity entity) {
        skill.applyToEntity(entity, this);
    }

    public void removeFromEntity(LivingEntity entity) {
        skill.removeFromEntity(entity, this);
    }

    //──Serialization────────────────────────────────────────────────────────

    public void write(ValueOutput output, RegistryAccess access) {
        NbtHelpers.writeIdentifier(output, "skill", skillId);
        skill.serializerHandler().write(data, output.child("data"), access);
    }

    //loads a saved instance, returns null if the skill no longer exists
    public static @Nullable SkillInstance<?> read(ValueInput input, RegistryAccess access) {
        Identifier skillId = NbtHelpers.readIdentifier(input, "skill");
        if (skillId == null) return null;
        Skill<?> skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, access);
        if (skill == null) return null;
        return readTyped(input, skillId, skill, access);
    }

    private static <D extends SkillData> SkillInstance<D> readTyped(ValueInput input, Identifier skillId, Skill<D> skill, RegistryAccess access) {
        Optional<ValueInput> dataInput = input.child("data");
        D data = dataInput.map(child -> skill.serializerHandler().read(child, access)).orElse(null);
        if (data == null) data = skill.newData(access);
        return new SkillInstance<>(skillId, skill, data);
    }

    //──Sync────────────────────────────────────────────────────────

    //the data is length prefixed so a skill the client does not know can be skipped
    public void encode(ByteBuf buf, RegistryAccess access) {
        ByteBufHelpers.encodeIdentifier(skillId, buf);
        ByteBuf dataBuf = Unpooled.buffer();
        skill.syncHandler().encode(data, dataBuf, access);
        buf.writeInt(dataBuf.readableBytes());
        buf.writeBytes(dataBuf);
    }

    //decodes an instance, returns null if the skill does not exist on this side
    public static @Nullable SkillInstance<?> decode(ByteBuf buf, RegistryAccess access) {
        Identifier skillId = ByteBufHelpers.decodeIdentifier(buf);
        int length = buf.readInt();
        ByteBuf dataBuf = buf.readSlice(length);
        Skill<?> skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, access);
        if (skill == null) return null;
        return decodeTyped(dataBuf, skillId, skill, access);
    }

    private static <D extends SkillData> SkillInstance<D> decodeTyped(ByteBuf buf, Identifier skillId, Skill<D> skill, RegistryAccess access) {
        D data = skill.newData(access);
        skill.syncHandler().decode(data, buf, access);
        return new SkillInstance<>(skillId, skill, data);
    }
}
