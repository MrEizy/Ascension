package net.zic.ascension.api.ascension.core.skill.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SkillHolder implements DataSourceInstance<SkillDataSource> {

    private final Map<Identifier, SkillInstance<?>> skills = new HashMap<>();
    private final Map<Identifier, Set<Identifier>> skillOwners = new HashMap<>();

    //sync state, only the dirty and removed skills are sent in a partial patch
    private final Set<Identifier> dirtySkills = new HashSet<>();
    private final Set<Identifier> removedSkills = new HashSet<>();

    //skills read from save, held until an owner adds the skill again
    private final Map<Identifier, SkillInstance<?>> cachedSkills = new HashMap<>();

    /**
     * adds a skill, if the skill is already held only the owner is added.
     * if the skill is cached the cached instance is used instead
     * @param instance the skill we are adding
     * @param owner the source of this skill
     * @return true -> added, false -> not added
     */
    public boolean addSkill(SkillInstance<?> instance, Identifier owner) {
        if (instance == null) return false;
        Identifier skill = instance.getSkillId();
        if (hasSkill(skill)) return addOwner(skill, owner);

        SkillInstance<?> cached = takeCachedSkill(skill);
        skills.put(skill, cached != null ? cached : instance);
        skillOwners.computeIfAbsent(skill, key -> new HashSet<>()).add(owner);

        removedSkills.remove(skill);
        dirtySkills.add(skill);
        return true;
    }

    //adds an owner to a skill that is already held
    public boolean addOwner(Identifier skill, Identifier owner) {
        if (!hasSkill(skill)) return false;
        skillOwners.computeIfAbsent(skill, key -> new HashSet<>()).add(owner);
        return true;
    }

    /**
     * removes a skill only if it has no owners left
     * @param skill the skill we want to remove
     * @param owner the source of the removal
     * @return true->removed, false -> not removed
     */
    public boolean removeSkill(Identifier skill, Identifier owner) {
        Set<Identifier> owners = skillOwners.get(skill);
        if (owners == null) return false;
        owners.remove(owner);

        if (!owners.isEmpty()) return false;

        skills.remove(skill);
        skillOwners.remove(skill);

        dirtySkills.remove(skill);
        removedSkills.add(skill);
        return true;
    }

    public Collection<Identifier> getSkills() {return skills.keySet();}

    public Collection<SkillInstance<?>> getSkillInstances() {return skills.values();}

    public boolean hasSkill(Identifier skill) {
        return skills.containsKey(skill);
    }

    public @Nullable SkillInstance<?> getSkill(Identifier skill) {
        return skills.get(skill);
    }

    //should be used if you changed a skills SkillData
    public void markSkillDirty(Identifier skill) {
        if (hasSkill(skill)) dirtySkills.add(skill);
    }

    //──Cached Data────────────────────────────────────────────────────────
    public void addCachedSkill(SkillInstance<?> instance) {
        cachedSkills.put(instance.getSkillId(), instance);
    }

    public boolean hasCachedSkill(Identifier skill) {
        return cachedSkills.containsKey(skill);
    }

    public @Nullable SkillInstance<?> takeCachedSkill(Identifier skill) {
        return cachedSkills.remove(skill);
    }

    public void clearCache() {
        cachedSkills.clear();
    }

    //──Raw Manipulation────────────────────────────────────────────────────────
    public Map<Identifier, SkillInstance<?>> getSkillSnapshot() {
        return Map.copyOf(skills);
    }

    public Map<Identifier, Set<Identifier>> getOwnerSnapshot() {
        Map<Identifier, Set<Identifier>> owners = new HashMap<>();
        skillOwners.forEach((skill, skillOwner) -> owners.put(skill, Set.copyOf(skillOwner)));
        return owners;
    }

    //restores skills without running any skill logic
    public void restore(Map<Identifier, SkillInstance<?>> rawSkills, Map<Identifier, Set<Identifier>> rawOwners) {
        skills.clear();
        skillOwners.clear();
        skills.putAll(rawSkills);
        rawOwners.forEach((skill, owners) -> skillOwners.put(skill, new HashSet<>(owners)));
        dirtySkills.addAll(rawSkills.keySet());
    }

    public void clearContainer() {
        skills.clear();
        skillOwners.clear();
        dirtySkills.clear();
        removedSkills.clear();
    }

    //──Sync────────────────────────────────────────────────────────

    void encodeFull(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(skills.size());
        for (SkillInstance<?> instance : skills.values()) {
            instance.encode(buf, access);
        }
        clearSyncState();
    }

    void encodePartial(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(dirtySkills.size());
        for (Identifier skill : dirtySkills) {
            skills.get(skill).encode(buf, access);
        }
        ByteBufHelpers.encodeCollection(removedSkills, buf, ByteBufHelpers::encodeIdentifier);
        clearSyncState();
    }

    void decodeFull(ByteBuf buf, RegistryAccess access) {
        skills.clear();
        decodeInstances(buf, access);
        clearSyncState();
    }

    void decodePartial(ByteBuf buf, RegistryAccess access) {
        decodeInstances(buf, access);
        ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier).forEach(skills::remove);
        clearSyncState();
    }

    private void decodeInstances(ByteBuf buf, RegistryAccess access) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            SkillInstance<?> instance = SkillInstance.decode(buf, access);
            if (instance != null) skills.put(instance.getSkillId(), instance);
        }
    }

    void clearSyncState() {
        dirtySkills.clear();
        removedSkills.clear();
    }

    @Override
    public SkillDataSource getDataSource() {
        return CoreDataSources.SKILL_DATA_SOURCE.get();
    }
}
