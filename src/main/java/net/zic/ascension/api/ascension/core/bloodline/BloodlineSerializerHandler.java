package net.zic.ascension.api.ascension.core.bloodline;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;

public class BloodlineSerializerHandler implements SerializerHandler<BloodlineHolder> {
    @Override
    public BloodlineHolder read(ValueInput input, RegistryAccess access) {
        BloodlineHolder holder = new BloodlineHolder();
        ValueInput.ValueInputList bloodlinesInput = input.childrenListOrEmpty("bloodlines");
        for (ValueInput bloodlineInput : bloodlinesInput) {
            try {
                BloodlineInstance<?> instance = BloodlineInstance.read(bloodlineInput, access);
                if (instance == null) continue;
                holder.addBloodline(instance);
            } catch (Exception exception) {
                AscensionCraft.LOGGER.debug("Error loading bloodline");
                AscensionCraft.LOGGER.debug("stacktrace: ", exception);
            }
        }
        return holder;
    }

    @Override
    public void write(BloodlineHolder writable, ValueOutput output, RegistryAccess access) {
        ValueOutput.ValueOutputList bloodlineOutputList = output.childrenList("bloodlines");
        for (BloodlineInstance<?> instance : writable.getBloodlineInstances()) {
            try {
                instance.write(bloodlineOutputList.addChild(), access);
            } catch (Exception exception) {
                AscensionCraft.LOGGER.error("Error writing bloodline {}", instance.getBloodlineId());
                AscensionCraft.LOGGER.error("stacktrace: ", exception);
            }
        }
    }
}
