package net.zic.ascension.mob_cultivation;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class MobCultivationData {
    private boolean initialized;
    private boolean cultivated;
    private MobCultivationCategory category = MobCultivationCategory.PASSIVE;
    private Identifier foundationPath;
    private int majorRealm;
    private int minorRealm;
    private double progress;

    private double generatedVitality;
    private double generatedStrength;
    private double generatedAgility;
    private double generatedSpirit;

    private transient boolean generatedStatsApplied;
    private transient boolean runtimeSessionInitialized;
    private transient long lastGrowthGameTime;
    private transient int lastProcessedHurtTimestamp = -1;
    private transient UUID retaliationTarget;
    private transient int retaliationTicks;
    private transient int retaliationAttackCooldown;
    private transient UUID fleeFrom;
    private transient int fleeTicks;
    private boolean debugNameApplied;
    private String lastDebugName;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isCultivated() {
        return cultivated;
    }

    public void setCultivated(boolean cultivated) {
        this.cultivated = cultivated;
    }

    public MobCultivationCategory getCategory() {
        return category;
    }

    public void setCategory(MobCultivationCategory category) {
        this.category = category == null ? MobCultivationCategory.PASSIVE : category;
    }

    public Identifier getFoundationPath() {
        return foundationPath;
    }

    public void setFoundationPath(Identifier foundationPath) {
        this.foundationPath = foundationPath;
    }

    public int getMajorRealm() {
        return majorRealm;
    }

    public int getMinorRealm() {
        return minorRealm;
    }

    public double getProgress() {
        return progress;
    }

    public void setPathState(int majorRealm, int minorRealm, double progress) {
        this.majorRealm = Math.max(0, majorRealm);
        this.minorRealm = Math.max(0, minorRealm);
        this.progress = Math.max(0.0D, progress);
    }

    public double getGeneratedVitality() {
        return generatedVitality;
    }

    public double getGeneratedStrength() {
        return generatedStrength;
    }

    public double getGeneratedAgility() {
        return generatedAgility;
    }

    public double getGeneratedSpirit() {
        return generatedSpirit;
    }

    public void setGeneratedStats(double vitality, double strength, double agility, double spirit) {
        generatedVitality = vitality;
        generatedStrength = strength;
        generatedAgility = agility;
        generatedSpirit = spirit;
    }

    public boolean areGeneratedStatsApplied() {
        return generatedStatsApplied;
    }

    public void setGeneratedStatsApplied(boolean generatedStatsApplied) {
        this.generatedStatsApplied = generatedStatsApplied;
    }

    public void clearGeneratedState() {
        foundationPath = null;
        setPathState(0, 0, 0.0D);
        setGeneratedStats(0.0D, 0.0D, 0.0D, 0.0D);
        generatedStatsApplied = false;
    }

    public long getLastGrowthGameTime() {
        return lastGrowthGameTime;
    }

    public void setLastGrowthGameTime(long lastGrowthGameTime) {
        this.lastGrowthGameTime = lastGrowthGameTime;
    }

    public int getLastProcessedHurtTimestamp() {
        return lastProcessedHurtTimestamp;
    }

    public void setLastProcessedHurtTimestamp(int timestamp) {
        lastProcessedHurtTimestamp = timestamp;
    }

    public UUID getRetaliationTarget() {
        return retaliationTarget;
    }

    public void startRetaliating(UUID target, int ticks) {
        retaliationTarget = target;
        retaliationTicks = ticks;
        retaliationAttackCooldown = 0;
    }

    public int getRetaliationTicks() {
        return retaliationTicks;
    }

    public void tickRetaliation(int elapsedTicks) {
        retaliationTicks = Math.max(0, retaliationTicks - elapsedTicks);
        retaliationAttackCooldown = Math.max(0, retaliationAttackCooldown - elapsedTicks);
    }

    public int getRetaliationAttackCooldown() {
        return retaliationAttackCooldown;
    }

    public void setRetaliationAttackCooldown(int retaliationAttackCooldown) {
        this.retaliationAttackCooldown = retaliationAttackCooldown;
    }

    public void stopRetaliating() {
        retaliationTarget = null;
        retaliationTicks = 0;
        retaliationAttackCooldown = 0;
    }

    public UUID getFleeFrom() {
        return fleeFrom;
    }

    public int getFleeTicks() {
        return fleeTicks;
    }

    public void startFleeing(UUID player, int ticks) {
        fleeFrom = player;
        fleeTicks = ticks;
    }

    public void tickFleeing(int elapsedTicks) {
        fleeTicks = Math.max(0, fleeTicks - elapsedTicks);
        if (fleeTicks == 0) {
            fleeFrom = null;
        }
    }

    public void stopFleeing() {
        fleeFrom = null;
        fleeTicks = 0;
    }

    public boolean isDebugNameApplied() {
        return debugNameApplied;
    }

    public String getLastDebugName() {
        return lastDebugName;
    }

    public void setDebugName(String debugName) {
        debugNameApplied = true;
        lastDebugName = debugName;
    }

    public void clearDebugNameState() {
        debugNameApplied = false;
        lastDebugName = null;
    }

    public void beginRuntimeSession(long gameTime) {
        if (runtimeSessionInitialized) {
            return;
        }
        runtimeSessionInitialized = true;
        lastGrowthGameTime = gameTime;
        lastProcessedHurtTimestamp = -1;
        stopRetaliating();
        stopFleeing();
    }

    public void endRuntimeSession() {
        runtimeSessionInitialized = false;
        stopRetaliating();
        stopFleeing();
    }

    private static MobCultivationCategory readCategory(String name) {
        try {
            return MobCultivationCategory.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return MobCultivationCategory.PASSIVE;
        }
    }

    private static Identifier readIdentifier(ValueInput input, String key) {
        String value = input.getStringOr(key, "");
        if (value.isBlank()) {
            return null;
        }

        try {
            return Identifier.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static final class Provider implements IAttachmentSerializer<MobCultivationData> {
        @Override
        public MobCultivationData read(@NonNull IAttachmentHolder holder, ValueInput input) {
            MobCultivationData data = new MobCultivationData();
            data.initialized = input.getBooleanOr("initialized", false);
            data.cultivated = input.getBooleanOr("cultivated", false);
            data.category = readCategory(input.getStringOr("category", MobCultivationCategory.PASSIVE.name()));
            data.foundationPath = readIdentifier(input, "foundation_path");
            data.majorRealm = input.getIntOr("major_realm", 0);
            data.minorRealm = input.getIntOr("minor_realm", 0);
            data.progress = input.getDoubleOr("progress", 0.0D);
            data.generatedVitality = input.getDoubleOr("generated_vitality", 0.0D);
            data.generatedStrength = input.getDoubleOr("generated_strength", 0.0D);
            data.generatedAgility = input.getDoubleOr("generated_agility", 0.0D);
            data.generatedSpirit = input.getDoubleOr("generated_spirit", 0.0D);
            data.debugNameApplied = input.getBooleanOr("debug_name_applied", false);
            data.lastDebugName = input.getStringOr("last_debug_name", "");
            if (data.lastDebugName.isBlank()) {
                data.lastDebugName = null;
            }
            return data;
        }

        @Override
        public boolean write(MobCultivationData attachment, ValueOutput output) {
            output.putBoolean("initialized", attachment.initialized);
            output.putBoolean("cultivated", attachment.cultivated);
            output.putString("category", attachment.category.name());

            if (attachment.foundationPath != null) {
                output.putString("foundation_path", attachment.foundationPath.toString());
            }
            output.putInt("major_realm", attachment.majorRealm);
            output.putInt("minor_realm", attachment.minorRealm);
            output.putDouble("progress", attachment.progress);

            output.putDouble("generated_vitality", attachment.generatedVitality);
            output.putDouble("generated_strength", attachment.generatedStrength);
            output.putDouble("generated_agility", attachment.generatedAgility);
            output.putDouble("generated_spirit", attachment.generatedSpirit);
            output.putBoolean("debug_name_applied", attachment.debugNameApplied);
            if (attachment.lastDebugName != null) {
                output.putString("last_debug_name", attachment.lastDebugName);
            }
            return true;
        }
    }
}
