package net.zic.ascension.mob_cultivation;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class MobCultivationData {
    private boolean initialized;
    private boolean cultivated;
    private MobCultivationCategory category = MobCultivationCategory.PASSIVE;
    private MobCultivationEliteTier eliteTier = MobCultivationEliteTier.NORMAL;
    private Identifier foundationPath;
    private final Set<Identifier> subPaths = new LinkedHashSet<>();
    private final Set<Identifier> traits = new LinkedHashSet<>();
    private final Set<Identifier> skillPools = new LinkedHashSet<>();
    private final Set<Identifier> assignedSkills = new LinkedHashSet<>();
    private Identifier lootProfile;
    private boolean growthFrozen;
    private int majorRealm;
    private int minorRealm;
    private double progress;

    private double generatedVitality;
    private double generatedStrength;
    private double generatedAgility;
    private double generatedSpirit;

    private transient boolean generatedStatsApplied;
    private transient long appliedProfileRevision = -1L;
    private transient boolean runtimeSessionInitialized;
    private transient long lastGrowthGameTime;
    private transient int lastProcessedHurtTimestamp = -1;
    private transient int lastPackAlertHurtTimestamp = -1;
    private transient UUID retaliationTarget;
    private transient int retaliationTicks;
    private transient int retaliationAttackCooldown;
    private transient UUID fleeFrom;
    private transient int fleeTicks;
    private transient long nextQiSeekGameTime;
    private transient long nextRegenerationGameTime;
    private boolean debugNameApplied;
    private String lastDebugName;

    public boolean isInitialized() { return initialized; }
    public void setInitialized(boolean initialized) { this.initialized = initialized; }
    public boolean isCultivated() { return cultivated; }
    public void setCultivated(boolean cultivated) { this.cultivated = cultivated; }
    public MobCultivationCategory getCategory() { return category; }
    public void setCategory(MobCultivationCategory category) {
        this.category = category == null ? MobCultivationCategory.PASSIVE : category;
    }
    public MobCultivationEliteTier getEliteTier() { return eliteTier; }
    public void setEliteTier(MobCultivationEliteTier eliteTier) {
        this.eliteTier = eliteTier == null ? MobCultivationEliteTier.NORMAL : eliteTier;
    }
    public Identifier getFoundationPath() { return foundationPath; }
    public void setFoundationPath(Identifier foundationPath) { this.foundationPath = foundationPath; }
    public Set<Identifier> getSubPaths() { return Set.copyOf(subPaths); }
    public void setSubPaths(Collection<Identifier> values) {
        subPaths.clear();
        if (values != null) values.stream().filter(java.util.Objects::nonNull).forEach(subPaths::add);
    }
    public boolean addSubPath(Identifier id) { return id != null && subPaths.add(id); }
    public boolean removeSubPath(Identifier id) { return id != null && subPaths.remove(id); }
    public Set<Identifier> getTraits() { return Set.copyOf(traits); }
    public void setTraits(Collection<Identifier> values) {
        traits.clear();
        if (values != null) values.stream().filter(java.util.Objects::nonNull).forEach(traits::add);
    }
    public boolean addTrait(Identifier id) { return id != null && traits.add(id); }
    public boolean removeTrait(Identifier id) { return id != null && traits.remove(id); }
    public boolean hasTrait(Identifier id) { return id != null && traits.contains(id); }
    public Set<Identifier> getSkillPools() { return Set.copyOf(skillPools); }
    public void setSkillPools(Collection<Identifier> values) {
        skillPools.clear();
        if (values != null) values.stream().filter(java.util.Objects::nonNull).forEach(skillPools::add);
    }
    public Set<Identifier> getAssignedSkills() { return Set.copyOf(assignedSkills); }
    public void setAssignedSkills(Collection<Identifier> values) {
        assignedSkills.clear();
        if (values != null) values.stream().filter(java.util.Objects::nonNull).forEach(assignedSkills::add);
    }
    public Identifier getLootProfile() { return lootProfile; }
    public void setLootProfile(Identifier lootProfile) { this.lootProfile = lootProfile; }
    public boolean isGrowthFrozen() { return growthFrozen; }
    public void setGrowthFrozen(boolean growthFrozen) { this.growthFrozen = growthFrozen; }
    public int getMajorRealm() { return majorRealm; }
    public int getMinorRealm() { return minorRealm; }
    public double getProgress() { return progress; }

    public void setPathState(int majorRealm, int minorRealm, double progress) {
        this.majorRealm = Math.max(0, majorRealm);
        this.minorRealm = Math.max(0, minorRealm);
        this.progress = Math.max(0.0D, progress);
    }

    public double getGeneratedVitality() { return generatedVitality; }
    public double getGeneratedStrength() { return generatedStrength; }
    public double getGeneratedAgility() { return generatedAgility; }
    public double getGeneratedSpirit() { return generatedSpirit; }
    public void setGeneratedStats(double vitality, double strength, double agility, double spirit) {
        generatedVitality = vitality;
        generatedStrength = strength;
        generatedAgility = agility;
        generatedSpirit = spirit;
    }
    public boolean areGeneratedStatsApplied() { return generatedStatsApplied; }
    public void setGeneratedStatsApplied(boolean generatedStatsApplied) { this.generatedStatsApplied = generatedStatsApplied; }
    public long getAppliedProfileRevision() { return appliedProfileRevision; }
    public void setAppliedProfileRevision(long revision) { appliedProfileRevision = revision; }

    public void clearGeneratedState() {
        foundationPath = null;
        subPaths.clear();
        traits.clear();
        skillPools.clear();
        assignedSkills.clear();
        lootProfile = null;
        eliteTier = MobCultivationEliteTier.NORMAL;
        setPathState(0, 0, 0.0D);
        setGeneratedStats(0.0D, 0.0D, 0.0D, 0.0D);
        generatedStatsApplied = false;
        appliedProfileRevision = -1L;
    }

    public long getLastGrowthGameTime() { return lastGrowthGameTime; }
    public void setLastGrowthGameTime(long value) { lastGrowthGameTime = value; }
    public int getLastProcessedHurtTimestamp() { return lastProcessedHurtTimestamp; }
    public void setLastProcessedHurtTimestamp(int value) { lastProcessedHurtTimestamp = value; }
    public int getLastPackAlertHurtTimestamp() { return lastPackAlertHurtTimestamp; }
    public void setLastPackAlertHurtTimestamp(int value) { lastPackAlertHurtTimestamp = value; }
    public UUID getRetaliationTarget() { return retaliationTarget; }
    public void startRetaliating(UUID target, int ticks) {
        retaliationTarget = target;
        retaliationTicks = ticks;
        retaliationAttackCooldown = 0;
    }
    public int getRetaliationTicks() { return retaliationTicks; }
    public void tickRetaliation(int elapsedTicks) {
        retaliationTicks = Math.max(0, retaliationTicks - elapsedTicks);
        retaliationAttackCooldown = Math.max(0, retaliationAttackCooldown - elapsedTicks);
    }
    public int getRetaliationAttackCooldown() { return retaliationAttackCooldown; }
    public void setRetaliationAttackCooldown(int value) { retaliationAttackCooldown = value; }
    public void stopRetaliating() {
        retaliationTarget = null;
        retaliationTicks = 0;
        retaliationAttackCooldown = 0;
    }
    public UUID getFleeFrom() { return fleeFrom; }
    public int getFleeTicks() { return fleeTicks; }
    public void startFleeing(UUID source, int ticks) { fleeFrom = source; fleeTicks = ticks; }
    public void tickFleeing(int elapsedTicks) {
        fleeTicks = Math.max(0, fleeTicks - elapsedTicks);
        if (fleeTicks == 0) fleeFrom = null;
    }
    public void stopFleeing() { fleeFrom = null; fleeTicks = 0; }
    public long getNextQiSeekGameTime() { return nextQiSeekGameTime; }
    public void setNextQiSeekGameTime(long value) { nextQiSeekGameTime = value; }
    public long getNextRegenerationGameTime() { return nextRegenerationGameTime; }
    public void setNextRegenerationGameTime(long value) { nextRegenerationGameTime = value; }
    public boolean isDebugNameApplied() { return debugNameApplied; }
    public String getLastDebugName() { return lastDebugName; }
    public void setDebugName(String debugName) { debugNameApplied = true; lastDebugName = debugName; }
    public void clearDebugNameState() { debugNameApplied = false; lastDebugName = null; }

    public void beginRuntimeSession(long gameTime) {
        if (runtimeSessionInitialized) return;
        runtimeSessionInitialized = true;
        lastGrowthGameTime = gameTime;
        lastProcessedHurtTimestamp = -1;
        lastPackAlertHurtTimestamp = -1;
        nextQiSeekGameTime = gameTime + 200L;
        nextRegenerationGameTime = gameTime + 100L;
        stopRetaliating();
        stopFleeing();
    }

    public void endRuntimeSession() {
        runtimeSessionInitialized = false;
        stopRetaliating();
        stopFleeing();
    }

    private static MobCultivationCategory readCategory(String name) {
        try { return MobCultivationCategory.valueOf(name); }
        catch (IllegalArgumentException exception) { return MobCultivationCategory.PASSIVE; }
    }

    private static Identifier readIdentifier(ValueInput input, String key) {
        String value = input.getStringOr(key, "");
        if (value.isBlank()) return null;
        try { return Identifier.parse(value); }
        catch (Exception ignored) { return null; }
    }

    private static Set<Identifier> readIdentifierSet(ValueInput input, String key) {
        Set<Identifier> result = new LinkedHashSet<>();
        for (ValueInput element : input.childrenListOrEmpty(key)) {
            String value = element.getStringOr("id", "");
            if (value.isBlank()) continue;
            try { result.add(Identifier.parse(value)); }
            catch (Exception ignored) { }
        }
        return result;
    }

    private static void writeIdentifierSet(ValueOutput output, String key, Collection<Identifier> values) {
        ValueOutput.ValueOutputList list = output.childrenList(key);
        for (Identifier id : values) {
            if (id == null) continue;
            ValueOutput child = list.addChild();
            child.putString("id", id.toString());
        }
    }

    public static final class Provider implements IAttachmentSerializer<MobCultivationData> {
        @Override
        public MobCultivationData read(@NonNull IAttachmentHolder holder, ValueInput input) {
            MobCultivationData data = new MobCultivationData();
            data.initialized = input.getBooleanOr("initialized", false);
            data.cultivated = input.getBooleanOr("cultivated", false);
            data.category = readCategory(input.getStringOr("category", MobCultivationCategory.PASSIVE.name()));
            data.eliteTier = MobCultivationEliteTier.parse(input.getStringOr("elite_tier", "normal"));
            data.foundationPath = readIdentifier(input, "foundation_path");
            data.subPaths.addAll(readIdentifierSet(input, "sub_paths"));
            data.traits.addAll(readIdentifierSet(input, "traits"));
            data.skillPools.addAll(readIdentifierSet(input, "skill_pools"));
            data.assignedSkills.addAll(readIdentifierSet(input, "assigned_skills"));
            data.lootProfile = readIdentifier(input, "loot_profile");
            data.growthFrozen = input.getBooleanOr("growth_frozen", false);
            data.majorRealm = input.getIntOr("major_realm", 0);
            data.minorRealm = input.getIntOr("minor_realm", 0);
            data.progress = input.getDoubleOr("progress", 0.0D);
            data.generatedVitality = input.getDoubleOr("generated_vitality", 0.0D);
            data.generatedStrength = input.getDoubleOr("generated_strength", 0.0D);
            data.generatedAgility = input.getDoubleOr("generated_agility", 0.0D);
            data.generatedSpirit = input.getDoubleOr("generated_spirit", 0.0D);
            data.debugNameApplied = input.getBooleanOr("debug_name_applied", false);
            data.lastDebugName = input.getStringOr("last_debug_name", "");
            if (data.lastDebugName.isBlank()) data.lastDebugName = null;
            return data;
        }

        @Override
        public boolean write(MobCultivationData data, ValueOutput output) {
            output.putBoolean("initialized", data.initialized);
            output.putBoolean("cultivated", data.cultivated);
            output.putString("category", data.category.name());
            output.putString("elite_tier", data.eliteTier.name());
            if (data.foundationPath != null) output.putString("foundation_path", data.foundationPath.toString());
            writeIdentifierSet(output, "sub_paths", data.subPaths);
            writeIdentifierSet(output, "traits", data.traits);
            writeIdentifierSet(output, "skill_pools", data.skillPools);
            writeIdentifierSet(output, "assigned_skills", data.assignedSkills);
            if (data.lootProfile != null) output.putString("loot_profile", data.lootProfile.toString());
            output.putBoolean("growth_frozen", data.growthFrozen);
            output.putInt("major_realm", data.majorRealm);
            output.putInt("minor_realm", data.minorRealm);
            output.putDouble("progress", data.progress);
            output.putDouble("generated_vitality", data.generatedVitality);
            output.putDouble("generated_strength", data.generatedStrength);
            output.putDouble("generated_agility", data.generatedAgility);
            output.putDouble("generated_spirit", data.generatedSpirit);
            output.putBoolean("debug_name_applied", data.debugNameApplied);
            if (data.lastDebugName != null) output.putString("last_debug_name", data.lastDebugName);
            return true;
        }
    }
}
