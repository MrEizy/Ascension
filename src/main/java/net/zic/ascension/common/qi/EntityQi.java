package net.zic.ascension.common.qi;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jspecify.annotations.NonNull;

/**
 * Temporary barebones Qi attatchment, will be replaced by a better method soon hopefully :)
 * TODO: ping Olli until he does it
 */
public class EntityQi {

    private double currentQi = 100.0D;
    private double maxQi = 100.0D;

    public double getCurrentQi() {
        return currentQi;
    }

    public double getMaxQi() {
        return maxQi;
    }

    public double getProgress() {
        if (maxQi <= 0.0D) {
            return 0.0D;
        }

        return Math.clamp(currentQi / maxQi, 0.0D, 1.0D);
    }

    public void setCurrentQi(double currentQi) {
        this.currentQi = Math.clamp(currentQi, 0.0D, maxQi);
    }

    public void setMaxQi(double maxQi) {
        this.maxQi = Math.max(0.0D, maxQi);
        this.currentQi = Math.clamp(currentQi, 0.0D, this.maxQi);
    }

    public void addQi(double amount) {
        setCurrentQi(currentQi + amount);
    }

    public void removeQi(double amount) {
        setCurrentQi(currentQi - amount);
    }

    public static class Serializer implements IAttachmentSerializer<EntityQi> {
        @Override
        public EntityQi read(@NonNull IAttachmentHolder holder, ValueInput input) {
            EntityQi qi = new EntityQi();

            qi.currentQi = input.getDoubleOr("current_qi", 100.0D);
            qi.maxQi = input.getDoubleOr("max_qi", 100.0D);

            qi.currentQi = Math.clamp(qi.currentQi, 0.0D, qi.maxQi);

            return qi;
        }

        @Override
        public boolean write(EntityQi qi, ValueOutput output) {
            output.putDouble("current_qi", qi.currentQi);
            output.putDouble("max_qi", qi.maxQi);
            return true;
        }
    }
}