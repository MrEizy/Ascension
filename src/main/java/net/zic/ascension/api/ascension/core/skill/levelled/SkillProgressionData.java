package net.zic.ascension.api.ascension.core.skill.levelled;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SkillProgressionData implements RegistryObjectData {
    public static final MapCodec<SkillProgressionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("trained_level", 0).forGetter(SkillProgressionData::getTrainedLevel),
            Codec.DOUBLE.optionalFieldOf("experience", 0.0D).forGetter(SkillProgressionData::getExperience),
            Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("level_floors", Map.of()).forGetter(SkillProgressionData::getLevelFloors),
            Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("level_caps", Map.of()).forGetter(SkillProgressionData::getLevelCaps)
    ).apply(instance, SkillProgressionData::new));

    private int trainedLevel;
    private double experience;
    private final Map<Identifier, Integer> levelFloors = new HashMap<>();
    private final Map<Identifier, Integer> levelCaps = new HashMap<>();

    public SkillProgressionData() {
    }

    public SkillProgressionData(
            int trainedLevel,
            double experience,
            Map<Identifier, Integer> levelFloors,
            Map<Identifier, Integer> levelCaps
    ) {
        this.trainedLevel = Math.max(0, trainedLevel);
        this.experience = Math.max(0.0D, experience);
        copyContributions(levelFloors, this.levelFloors);
        copyContributions(levelCaps, this.levelCaps);
    }

    public SkillProgressionData(ValueInput input) {
        trainedLevel = Math.max(0, input.getIntOr("trained_level", 0));
        experience = Math.max(0.0D, input.getDoubleOr("experience", 0.0D));
        readContributions(input.childrenListOrEmpty("level_floors"), levelFloors);
        readContributions(input.childrenListOrEmpty("level_caps"), levelCaps);
    }

    public SkillProgressionData(ByteBuf buf) {
        trainedLevel = Math.max(0, buf.readInt());
        experience = Math.max(0.0D, buf.readDouble());
        readContributions(buf, levelFloors);
        readContributions(buf, levelCaps);
    }

    public int getTrainedLevel() {
        return trainedLevel;
    }

    public void setTrainedLevel(int trainedLevel) {
        this.trainedLevel = Math.max(0, trainedLevel);
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = Math.max(0.0D, experience);
    }

    public Map<Identifier, Integer> getLevelFloors() {
        return Collections.unmodifiableMap(levelFloors);
    }

    public Map<Identifier, Integer> getLevelCaps() {
        return Collections.unmodifiableMap(levelCaps);
    }

    public int getLevelFloor() {
        return maximumContribution(levelFloors, 0);
    }

    public int getAccessibleLevelCap(int defaultAccessibleLevel, int maximumLevel) {
        int contributedCap = maximumContribution(levelCaps, defaultAccessibleLevel);
        return clamp(contributedCap, 0, maximumLevel);
    }

    public boolean setLevelFloor(Identifier contributionId, int level) {
        return setContribution(levelFloors, contributionId, level);
    }

    public boolean removeLevelFloor(Identifier contributionId) {
        return contributionId != null && levelFloors.remove(contributionId) != null;
    }

    public boolean setLevelCap(Identifier contributionId, int level) {
        return setContribution(levelCaps, contributionId, level);
    }

    public boolean removeLevelCap(Identifier contributionId) {
        return contributionId != null && levelCaps.remove(contributionId) != null;
    }

    public boolean removeContribution(Identifier contributionId) {
        boolean removedFloor = removeLevelFloor(contributionId);
        boolean removedCap = removeLevelCap(contributionId);
        return removedFloor || removedCap;
    }

    @Override
    public void write(ValueOutput output) {
        output.putInt("trained_level", trainedLevel);
        output.putDouble("experience", experience);
        writeContributions(output.childrenList("level_floors"), levelFloors);
        writeContributions(output.childrenList("level_caps"), levelCaps);
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(trainedLevel);
        buf.writeDouble(experience);
        writeContributions(buf, levelFloors);
        writeContributions(buf, levelCaps);
    }

    private static void copyContributions(Map<Identifier, Integer> source, Map<Identifier, Integer> target) {
        if (source == null) {
            return;
        }
        source.forEach((id, level) -> {
            if (id != null && level != null) {
                target.put(id, Math.max(0, level));
            }
        });
    }

    private static boolean setContribution(Map<Identifier, Integer> contributions, Identifier contributionId, int level) {
        if (contributionId == null) {
            return false;
        }
        int clampedLevel = Math.max(0, level);
        Integer previous = contributions.put(contributionId, clampedLevel);
        return previous == null || previous != clampedLevel;
    }

    private static int maximumContribution(Map<Identifier, Integer> contributions, int fallback) {
        int maximum = fallback;
        for (int value : contributions.values()) {
            maximum = Math.max(maximum, value);
        }
        return maximum;
    }

    private static void writeContributions(ValueOutput.ValueOutputList output, Map<Identifier, Integer> contributions) {
        contributions.forEach((id, level) -> {
            ValueOutput contributionOutput = output.addChild();
            NbtHelpers.writeIdentifier(contributionOutput, "id", id);
            contributionOutput.putInt("level", level);
        });
    }

    private static void readContributions(ValueInput.ValueInputList input, Map<Identifier, Integer> contributions) {
        for (ValueInput contributionInput : input) {
            Identifier id = NbtHelpers.readIdentifier(contributionInput, "id");
            if (id != null) {
                contributions.put(id, Math.max(0, contributionInput.getIntOr("level", 0)));
            }
        }
    }

    private static void writeContributions(ByteBuf buf, Map<Identifier, Integer> contributions) {
        buf.writeInt(contributions.size());
        contributions.forEach((id, level) -> {
            ByteBufHelpers.encodeIdentifier(id, buf);
            buf.writeInt(level);
        });
    }

    private static void readContributions(ByteBuf buf, Map<Identifier, Integer> contributions) {
        int size = buf.readInt();
        for (int index = 0; index < size; index++) {
            Identifier id = ByteBufHelpers.decodeIdentifier(buf);
            int level = Math.max(0, buf.readInt());
            contributions.put(id, level);
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
