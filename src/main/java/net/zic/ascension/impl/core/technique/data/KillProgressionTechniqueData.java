package net.zic.ascension.impl.core.technique.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;

public final class KillProgressionTechniqueData implements TechniqueData {
    private int totalKills;
    private double totalProgressEarned;

    public KillProgressionTechniqueData() {
        this(0, 0.0D);
    }

    public KillProgressionTechniqueData(int totalKills, double totalProgressEarned) {
        this.totalKills = Math.max(0, totalKills);
        this.totalProgressEarned = sanitizeNonNegative(totalProgressEarned);
    }

    public int getTotalKills() {
        return totalKills;
    }

    public double getTotalProgressEarned() {
        return totalProgressEarned;
    }

    public void recordKill(double progressEarned) {
        if (totalKills < Integer.MAX_VALUE) {
            totalKills++;
        }
        totalProgressEarned = Math.min(
                Double.MAX_VALUE,
                totalProgressEarned + sanitizeNonNegative(progressEarned)
        );
    }

    @Override
    public void write(ValueOutput output) {
        output.putInt("total_kills", totalKills);
        output.putDouble("total_progress_earned", totalProgressEarned);
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(totalKills);
        buf.writeDouble(totalProgressEarned);
    }

    public static KillProgressionTechniqueData load(ValueInput input) {
        return new KillProgressionTechniqueData(
                input.getIntOr("total_kills", 0),
                input.getDoubleOr("total_progress_earned", 0.0D)
        );
    }

    public static KillProgressionTechniqueData decode(ByteBuf buf) {
        return new KillProgressionTechniqueData(buf.readInt(), buf.readDouble());
    }

    private static double sanitizeNonNegative(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 0.0D;
    }
}
