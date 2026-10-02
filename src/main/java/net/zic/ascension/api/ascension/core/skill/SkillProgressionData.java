package net.zic.ascension.api.ascension.core.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SkillProgressionData {
    public static final MapCodec<SkillProgressionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("trained", 1).forGetter(SkillProgressionData::getTrainedProgression),
            Codec.DOUBLE.optionalFieldOf("experience", 0.0D).forGetter(SkillProgressionData::getExperience),
            Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("caps", Map.of()).forGetter(SkillProgressionData::getCaps)
    ).apply(instance, SkillProgressionData::new));

    private int trainedProgression;
    private double experience;
    private final Map<Identifier, Integer> caps = new HashMap<>();

    public SkillProgressionData() {
        this(1, 0.0D, Map.of());
    }

    public SkillProgressionData(int trainedProgression, double experience, Map<Identifier, Integer> caps) {
        this.trainedProgression = Math.max(1, trainedProgression);
        this.experience = Math.max(0.0D, experience);
        copyContributions(caps, this.caps);
    }

    public SkillProgressionData(ValueInput input) {
        trainedProgression = Math.max(1, input.getIntOr("trained", 1));
        experience = Math.max(0.0D, input.getDoubleOr("experience", 0.0D));
        readContributions(input.childrenListOrEmpty("caps"), caps);
    }

    public SkillProgressionData(ByteBuf buf) {
        trainedProgression = Math.max(1, buf.readInt());
        experience = Math.max(0.0D, buf.readDouble());
        readContributions(buf, caps);
    }

    public int getTrainedProgression() {
        return trainedProgression;
    }

    public void setTrainedProgression(int trainedProgression) {
        this.trainedProgression = Math.max(1, trainedProgression);
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = Math.max(0.0D, experience);
    }

    public Map<Identifier, Integer> getCaps() {
        return Collections.unmodifiableMap(caps);
    }

    public int getAccessibleCap(int defaultCap, int maximumProgression) {
        int contributedCap = maximumContribution(caps, defaultCap);
        return clamp(contributedCap, 1, maximumProgression);
    }

    public boolean setCap(Identifier contributionId, int progression) {
        return setContribution(caps, contributionId, progression);
    }

    public boolean removeCap(Identifier contributionId) {
        return contributionId != null && caps.remove(contributionId) != null;
    }

    public void write(ValueOutput output) {
        output.putInt("trained", trainedProgression);
        output.putDouble("experience", experience);
        writeContributions(output.childrenList("caps"), caps);
    }

    public void encode(ByteBuf buf) {
        buf.writeInt(trainedProgression);
        buf.writeDouble(experience);
        writeContributions(buf, caps);
    }

    private static void copyContributions(Map<Identifier, Integer> source, Map<Identifier, Integer> target) {
        if (source == null) {
            return;
        }
        source.forEach((id, progression) -> {
            if (id != null && progression != null) {
                target.put(id, Math.max(1, progression));
            }
        });
    }

    private static boolean setContribution(Map<Identifier, Integer> contributions, Identifier contributionId, int progression) {
        if (contributionId == null) {
            return false;
        }
        int resolved = Math.max(1, progression);
        Integer previous = contributions.put(contributionId, resolved);
        return previous == null || previous != resolved;
    }

    private static int maximumContribution(Map<Identifier, Integer> contributions, int fallback) {
        int maximum = fallback;
        for (int value : contributions.values()) {
            maximum = Math.max(maximum, value);
        }
        return maximum;
    }

    private static void writeContributions(ValueOutput.ValueOutputList output, Map<Identifier, Integer> contributions) {
        contributions.forEach((id, progression) -> {
            ValueOutput contributionOutput = output.addChild();
            NbtHelpers.writeIdentifier(contributionOutput, "id", id);
            contributionOutput.putInt("progression", progression);
        });
    }

    private static void readContributions(ValueInput.ValueInputList input, Map<Identifier, Integer> contributions) {
        for (ValueInput contributionInput : input) {
            Identifier id = NbtHelpers.readIdentifier(contributionInput, "id");
            if (id != null) {
                contributions.put(id, Math.max(1, contributionInput.getIntOr("progression", 1)));
            }
        }
    }

    private static void writeContributions(ByteBuf buf, Map<Identifier, Integer> contributions) {
        buf.writeInt(contributions.size());
        contributions.forEach((id, progression) -> {
            ByteBufHelpers.encodeIdentifier(id, buf);
            buf.writeInt(progression);
        });
    }

    private static void readContributions(ByteBuf buf, Map<Identifier, Integer> contributions) {
        int size = buf.readInt();
        for (int index = 0; index < size; index++) {
            Identifier id = ByteBufHelpers.decodeIdentifier(buf);
            int progression = Math.max(1, buf.readInt());
            contributions.put(id, progression);
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
